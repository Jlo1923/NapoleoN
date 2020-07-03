package com.naposystems.pepito.ui.attachmentLocation

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentLocationFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.model.attachment.location.Place
import com.naposystems.pepito.ui.attachmentLocation.adapter.AttachmentLocationAdapter
import com.naposystems.pepito.ui.custom.SearchView
import com.naposystems.pepito.ui.custom.attachmentLocationBottomSheet.AttachmentLocationBottomSheet
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.pepito.utility.toBounds
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AttachmentLocationFragment : Fragment(), SearchView.OnSearchView,
    GoogleMap.SnapshotReadyCallback, AttachmentLocationAdapter.AttachmentLocationListener {

    companion object {
        private const val URL = "https://maps.google.com/maps"
        private const val ZOOM = 17.0f
        private const val ZOOM_OUT = 15.0f
        private const val ANIMATION_DURATION: Long = 250
        private const val DISTANCE_SEARCH_PLACE = (100 * 1000).toDouble() // 100 Km
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: AttachmentLocationViewModel by viewModels { viewModelFactory }
    private val conversationShareViewModel: ConversationShareViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: AttachmentLocationFragmentBinding
    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private var currentAddress: Address? = null
    private var currentPlace: Place? = null
    private val overshootInterpolator = OvershootInterpolator()
    private lateinit var searchView: SearchView
    private lateinit var mainActivity: MainActivity
    private val adapter: AttachmentLocationAdapter by lazy {
        AttachmentLocationAdapter(this)
    }
    private val placesClient: PlacesClient by lazy {
        Places.createClient(requireContext())
    }
    private val token: AutocompleteSessionToken by lazy {
        AutocompleteSessionToken.newInstance()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap

        initializeFusedLocationClient()

        googleMap.uiSettings.isMyLocationButtonEnabled = false

        googleMap.setOnCameraMoveStartedListener {

            binding.imageViewMarker.animate()
                .translationY(-75f)
                .setInterpolator(overshootInterpolator)
                .setDuration(ANIMATION_DURATION)
                .start()

            binding.bottomSheet.hide()
        }

        googleMap.setOnCameraIdleListener {
            binding.imageViewMarker.animate()
                .translationY(0f)
                .setInterpolator(overshootInterpolator)
                .setDuration(ANIMATION_DURATION)
                .start()

            setCurrentLocation(googleMap.cameraPosition.target)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.attachment_location_fragment, container, false
        )

        binding.bottomSheet.setListener(object :
            AttachmentLocationBottomSheet.AttachmentLocationBottomSheetListener {
            override fun onFabClicked() {
                takeMapSnapshot()
            }
        })

        binding.recyclerViewPlace.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.address.observe(viewLifecycleOwner, Observer { address ->
            if (address != null) {
                currentAddress = address
                binding.bottomSheet.showResult(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    addressToShortString = addressToShortString(address),
                    addressToString = addressToString(address)
                )
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_attachment_location, menu)
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setStyleable(Constants.LocationSearchView.LOCATION.location)
            searchView.setMenuItem(menu.findItem(R.id.search))
            searchView.setListener(this)
        }
    }

    private fun setCurrentLocation(location: LatLng) {
        currentLocation = location
        binding.bottomSheet.showLoading()
        if (currentPlace != null) {
            binding.bottomSheet.showResult(
                latitude = location.latitude,
                longitude = location.longitude,
                addressToShortString = currentPlace?.name ?: "",
                addressToString = currentPlace?.address ?: ""
            )
        } else {
            viewModel.getAddress(location)
        }
    }

    private fun initializeFusedLocationClient() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context as MainActivity)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    moveMapToPosition(LatLng(it.latitude, it.longitude))
                }
            }
            .addOnFailureListener { error ->
                Timber.e(error)
            }
    }

    private fun moveMapToPosition(latLng: LatLng) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM))
        setCurrentLocation(latLng)
    }

    private fun addressToString(address: Address?): String {
        return if (address != null) address.getAddressLine(0) else ""
    }

    private fun addressToShortString(address: Address?): String {
        if (address == null) return ""
        val addressLine = address.getAddressLine(0)
        val split = addressLine.split(",").toTypedArray()
        return when {
            split.size >= 3 -> {
                split[1].trim { it <= ' ' } + ", " + split[2].trim { it <= ' ' }
            }
            split.size == 2 -> {
                split[1].trim { it <= ' ' }
            }
            else -> split[0].trim { it <= ' ' }
        }
    }

    private fun takeMapSnapshot() {
        currentLocation?.let { currentLocation ->
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, ZOOM_OUT))
            googleMap?.addMarker(
                MarkerOptions().position(currentLocation)
            )
            googleMap?.snapshot(this)
        }
    }

    private fun searchPlace(text: String) {
        currentLocation?.let { currentLocation ->
            val request: FindAutocompletePredictionsRequest =
                FindAutocompletePredictionsRequest.builder()
                    .setLocationRestriction(
                        RectangularBounds.newInstance(
                            currentLocation.toBounds(
                                DISTANCE_SEARCH_PLACE
                            )
                        )
                    )
                    .setOrigin(currentLocation)
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(text)
                    .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    if (response.autocompletePredictions.isNotEmpty()) {
                        val listOfPlaces: List<Place> = response.autocompletePredictions.map {
                            val place = Place(
                                id = it.placeId,
                                name = it.getPrimaryText(null).toString(),
                                address = it.getSecondaryText(null).toString(),
                                distanceInMeters = it.distanceMeters ?: 0
                            )
                            place
                        }

                        if (binding.viewSwitcher.nextView.id == binding.recyclerViewPlace.id) {
                            binding.viewSwitcher.showNext()
                        }

                        adapter.submitList(listOfPlaces)
                    }
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        val apiException: ApiException = exception
                        Timber.e(apiException, "Place not found")
                    }
                }
        }
    }

    private fun showMap() {
        if (binding.viewSwitcher.nextView.id == binding.containerMap.id) {
            binding.viewSwitcher.showNext()
        }
    }

    //region Implementation GoogleMap.SnapshotReadyCallback
    override fun onSnapshotReady(nullableBitmap: Bitmap?) {
        nullableBitmap?.let { bitmap ->
            context?.let { context ->

                val file = FileManager.createFileFromBitmap(
                    context = context,
                    fileName = "${System.currentTimeMillis()}.jpg",
                    folder = Constants.NapoleonCacheDirectories.IMAGES.folder,
                    bitmap = bitmap
                )

                if (file != null) {

                    val attachment = Attachment(
                        id = 0,
                        messageId = 0,
                        webId = "",
                        messageWebId = "",
                        type = Constants.AttachmentType.LOCATION.type,
                        body = "",
                        uri = file.name,
                        origin = Constants.AttachmentOrigin.LOCATION.origin,
                        thumbnailUri = "",
                        status = Constants.AttachmentStatus.SENDING.status,
                        extension = "jpg",
                        duration = 0L
                    )

                    with(conversationShareViewModel) {

                        var message = String.format(
                            Locale.getDefault(),
                            "%s, %s \n",
                            binding.bottomSheet.getPlaceName(),
                            binding.bottomSheet.getPlaceAddress()
                        )

                        message += Uri.parse(URL)
                            .buildUpon()
                            .appendQueryParameter(
                                "q",
                                String.format(
                                    "%s,%s",
                                    currentAddress?.latitude ?: "",
                                    currentAddress?.longitude ?: ""
                                )
                            )
                            .build().toString()


                        setMessage(message)
                        setAttachmentSelected(attachment)
                        resetAttachmentSelected()
                        resetMessage()
                    }
                    findNavController().popBackStack(R.id.conversationFragment, false)

                } else {
                    this.showToast("Ha ocurrido un error al capturar el mapa|!!")
                }

            }
        }
    }
    //endregion

    //region Implementation SearchView.OnSearchView
    override fun onOpened() {
        // Intentionally empty
    }

    override fun onQuery(text: String) {
        if (text.isNotEmpty()) {
            searchPlace(text)
        } else {
            showMap()
        }
    }

    override fun onClosed() {
        showMap()
    }

    override fun onClosedCompleted() {}
    //endregion

    //region Implementation AttachmentLocationAdapter.AttachmentLocationListener
    override fun onPlaceSelected(place: Place) {
        // Specify the fields to return.
        val placeFields: List<Field> = listOf(LAT_LNG)

        // Construct a request object, passing the place ID and fields array.
        val request: FetchPlaceRequest = FetchPlaceRequest.newInstance(place.id, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val googlePlace: com.google.android.libraries.places.api.model.Place =
                    response.place
                googlePlace.latLng?.let { latLng ->
                    this.currentPlace = place
                    moveMapToPosition(latLng)
                    searchView.showSearchView()
                }
            }.addOnFailureListener { exception: java.lang.Exception ->
                if (exception is ApiException) {
                    val apiException = exception as ApiException
                    val statusCode = apiException.statusCode
                    // Handle error with given status code.
                    Timber.e("Place not found: " + exception.message)
                    this.showToast("No se pudo encontrar el sitio|!!")
                }
            }
    }
    //endregion
}