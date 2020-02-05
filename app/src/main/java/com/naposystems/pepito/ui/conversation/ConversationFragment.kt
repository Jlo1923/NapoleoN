package com.naposystems.pepito.ui.conversation

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationActionBarBinding
import com.naposystems.pepito.databinding.ConversationFragmentBinding
import com.naposystems.pepito.ui.attachment.AttachmentDialogFragment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.previewImageSend.SharePreviewImageSendViewModel
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.io.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

class ConversationFragment : Fragment() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        fun newInstance() = ConversationFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: ConversationViewModel
    private lateinit var shareViewModel: SharePreviewImageSendViewModel
    private lateinit var actionBarCustomView: ConversationActionBarBinding
    private lateinit var binding: ConversationFragmentBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var fileName: String
    private lateinit var compressFileName: String
    private val args: ConversationFragmentArgs by navArgs()
    private var isEditTextFilled: Boolean = false
    private val subFolder by lazy {
        "conversations/${viewModel.getUser().id}_${args.contact.id}"
    }
    private lateinit var path: File
    private lateinit var file: Bitmap

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        inflateCustomActionBar(inflater)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_fragment, container, false
        )

        binding.contact = args.contact

        setupAdapter()

        Handler().postDelayed({
            binding.textViewUserStatus.isSelected = true
        }, TimeUnit.SECONDS.toMillis(2))

        binding.inputPanel.getFloatingActionButton().setOnClickListener {

            if (!binding.inputPanel.getFloatingActionButton().isShowingMic()) {
                viewModel.saveMessageLocally(
                    binding.inputPanel.getEditTex().text.toString(),
                    "",
                    args.contact,
                    Constants.IsMine.YES.value
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
        }

        binding.inputPanel.setEditTextWatcher(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
                val text = binding.inputPanel.getEditTex().text.toString()

                if (text.isNotEmpty() && !isEditTextFilled) {
                    binding.inputPanel.morphFloatingActionButtonIcon()
                    isEditTextFilled = true
                    binding.inputPanel.hideImageButtonCamera()
                } else if (text.isEmpty()) {
                    binding.inputPanel.morphFloatingActionButtonIcon()
                    isEditTextFilled = false
                    binding.inputPanel.showImageButtonCamera()
                }

            }
        })

        binding.inputPanel.getImageButtonAttachment().setOnClickListener {
            val attachmentDialog = AttachmentDialogFragment()
            attachmentDialog.show(parentFragmentManager, "attachmentDialog")
        }

        binding.inputPanel.getImageButtonCamera().setOnClickListener {
            verifyCameraAndMediaPermission()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shareViewModel = ViewModelProviders.of(activity!!, viewModelFactory)
            .get(SharePreviewImageSendViewModel::class.java)

        shareViewModel.hasCancelClicked.observe(activity!!, Observer {
            if (it == true) {
                deleteImages()
            }
        })

        shareViewModel.message.observe(activity!!, Observer {
            if (it.isNotEmpty()) {
                val base64 = Utils.bitmapToBase64(file)

                viewModel.saveMessageWithAttachmentLocally(
                    it,
                    "",
                    args.contact,
                    Constants.IsMine.YES.value,
                    base64,
                    "${path}/${fileName}_compress.jpg"
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
                shareViewModel.resetMessage()
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ConversationViewModel::class.java)

        viewModel.setContact(args.contact)

        viewModel.getLocalMessages()

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        viewModel.messageMessages.observe(viewLifecycleOwner, Observer { conversationList ->

            if (conversationList.isNotEmpty()) {

                viewModel.sendMessagesRead()

                /*val mutableList: MutableList<Conversation> = ArrayList()

                mutableList.addAll(conversationList.sortedBy { it.id })

                val conversationGrouped = mutableList.groupBy {
                    if (it.createdAt.isNotEmpty()) {
                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val calendar = Calendar.getInstance(TimeZone.getDefault())
                        calendar.timeInMillis = it.createdAt.toLong() * 1000
                        calendar.set(Calendar.HOUR, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.time
                    }
                }

                for (groupedKey in conversationGrouped.entries) {
                    val indexFirst = mutableList.indexOf(groupedKey.value[0])

                    Timber.d(indexFirst.toString())
                }*/

                adapter.submitList(conversationList)

                Handler().postDelayed({
                    if (adapter.itemCount > 0) {
                        val smoothScroller: RecyclerView.SmoothScroller =
                            object : LinearSmoothScroller(context) {
                                override fun getVerticalSnapPreference(): Int {
                                    return SNAP_TO_START
                                }
                            }

                        smoothScroller.targetPosition = 0

                        layoutManager.startSmoothScroll(smoothScroller)
                    }
                }, 300)

            }
        })
    }

    override fun onDetach() {
        (activity as MainActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        (activity as MainActivity).supportActionBar?.setDisplayShowCustomEnabled(false)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_conversation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_see_contact -> {
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToContactProfileFragment(args.contact.id)
                )
            }
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    path = File(context!!.externalCacheDir!!, subFolder)
                    val image = File(path, "${fileName}.jpg")
                    file = decodeFile(image)

                    findNavController().navigate(
                        ConversationFragmentDirections.actionConversationFragmentToPreviewImageSendFragment(
                            file
                        )
                    )
                }
            }
        }
    }

    private fun inflateCustomActionBar(inflater: LayoutInflater) {
        actionBarCustomView = DataBindingUtil.inflate(
            inflater, R.layout.conversation_action_bar, null, false
        )

        with((activity as MainActivity).supportActionBar!!) {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHasOptionsMenu(true)
            customView = actionBarCustomView.root
        }

        actionBarCustomView.contact = args.contact

        actionBarCustomView.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAdapter() {
        adapter = ConversationAdapter(ConversationAdapter.ConversationClickListener {
            Toast.makeText(context!!, it.message.body, Toast.LENGTH_SHORT).show()
        })

        layoutManager = LinearLayoutManager(context!!)
        layoutManager.reverseLayout = true

        binding.recyclerViewConversation.adapter = adapter
        binding.recyclerViewConversation.layoutManager = layoutManager
    }

    private fun verifyCameraAndMediaPermission() {

        Dexter.withActivity(activity!!)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        fileName = System.currentTimeMillis().toString()
                        compressFileName = "${fileName}_compress.jpg"
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Utils.getCacheImagePath(context!!, "${fileName}.jpg", subFolder)
                        )
                        if (takePictureIntent.resolveActivity(context!!.packageManager) != null) {
                            startActivityForResult(
                                takePictureIntent,
                                REQUEST_IMAGE_CAPTURE
                            )
                        }
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        Utils.showDialogToInformPermission(
                            context!!,
                            childFragmentManager,
                            R.drawable.ic_camera_primary,
                            R.string.explanation_camera_and_storage_permission,
                            { Utils.openSetting(context!!) },
                            {}
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Utils.showDialogToInformPermission(
                        context!!,
                        childFragmentManager,
                        R.drawable.ic_camera_primary,
                        R.string.explanation_camera_and_storage_permission,
                        { token!!.continuePermissionRequest() },
                        { token!!.cancelPermissionRequest() }
                    )
                }
            }).check()
    }

    private fun decodeFile(f: File): Bitmap {
        var b: Bitmap? = null

        //Decode image size
        val o: BitmapFactory.Options = BitmapFactory.Options()
        o.inJustDecodeBounds = true

        var fis: FileInputStream

        try {
            fis = FileInputStream(f)
            BitmapFactory.decodeStream(fis, null, o)
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val IMAGE_MAX_SIZE = 1024
        var scale = 1
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = 2.0.pow(
                ceil(
                    ln(
                        IMAGE_MAX_SIZE / max(
                            o.outHeight,
                            o.outWidth
                        ).toDouble()
                    ).toInt() / ln(0.5)
                )
            ).toInt()
        }

        //Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        try {
            fis = FileInputStream(f)
            b = BitmapFactory.decodeStream(fis, null, o2)
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val destFile = File(path, compressFileName)
        try {
            val out = FileOutputStream(destFile)
            b!!.compress(Bitmap.CompressFormat.PNG, 80, out)
            out.flush()
            out.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return b!!
    }

    private fun deleteImages() {
        val path = File(context!!.externalCacheDir, subFolder)
        if (path.exists() && path.isDirectory) {
            val fileOriginalImage = File(path, "${fileName}.jpg")
            val compressImage = File(path, compressFileName)

            if (fileOriginalImage.exists()) {
                fileOriginalImage.delete()
            }

            if (compressImage.exists()) {
                compressImage.delete()
            }
        }
    }
}
