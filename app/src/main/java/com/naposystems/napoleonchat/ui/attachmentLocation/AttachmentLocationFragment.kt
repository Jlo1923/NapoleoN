package com.naposystems.napoleonchat.ui.attachmentLocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import com.google.android.material.snackbar.Snackbar
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentLocationFragmentBinding
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.model.attachment.location.Place
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.ui.attachmentLocation.adapter.AttachmentLocationAdapter
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.custom.attachmentLocationBottomSheet.AttachmentLocationBottomSheet
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.adapters.showToast
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class AttachmentLocationFragment : Fragment(), SearchView.OnSearchView,
    AttachmentLocationAdapter.AttachmentLocationListener {

    companion object {
        private const val URL = "https://maps.google.com/maps"
        private const val ZOOM = 17.0f
        private const val ZOOM_OUT = 15.0f
        private const val ANIMATION_DURATION: Long = 250
        private const val DISTANCE_SEARCH_PLACE = (100 * 1000).toDouble() // 100 Km

        private const val REQUEST_LOCATION = 999
        private const val LAT = 0.6220584341398311
        private const val LNG = -22.189338840544224
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: AttachmentLocationViewModel by viewModels { viewModelFactory }
    private val conversationShareViewModel: ConversationShareViewModel by activityViewModels()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var binding: AttachmentLocationFragmentBinding
    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private var currentAddress: Address? = null
    private var currentPlace: Place? = null
    private val overshootInterpolator = OvershootInterpolator()
    private lateinit var searchView: SearchView
    private lateinit var mainActivity: MainActivity
    private lateinit var snapReadyCallback: GoogleMap.SnapshotReadyCallback
    private lateinit var mapLoadedCallback: OnMapLoadedCallback
    private val args: AttachmentLocationFragmentArgs by navArgs()

    private val adapter: AttachmentLocationAdapter by lazy {
        AttachmentLocationAdapter(this)
    }
    private val placesClient: PlacesClient by lazy {
        Places.createClient(requireContext())
    }
    private val token: AutocompleteSessionToken by lazy {
        AutocompleteSessionToken.newInstance()
    }
    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private var googleApiClient: GoogleApiClient? = null

    private var locationCallback: LocationCallback? = null

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
            Timber.d("*TestLocation: googleMap ${googleMap.cameraPosition.target}")
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
    ): View {

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

        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (args.contactId == it.contactId)
                        findNavController().popBackStack(R.id.homeFragment, false)
                }

        disposable.add(disposableContactBlockOrDelete)

        validateGpsEnable()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.address.observe(viewLifecycleOwner, { address ->
            if (address != null) {
                currentAddress = address
                binding.bottomSheet.showResult(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    addressToShortString = addressToShortString(address),
                    addressToString = addressToString(address)
                )
            } else {
                Timber.d("*TestLocation: address observer null")
                initializeFusedLocationClient()
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

    override fun onStop() {
        super.onStop()
        stopLocation()
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun setCurrentLocation(location: LatLng) {
        Timber.d("*TestLocation: setCurrentLocation $location")
        currentLocation = location
        binding.bottomSheet.showLoading()
        if (currentPlace != null) {
            Timber.d("*TestLocation: currentPlace not null")
            binding.bottomSheet.showResult(
                latitude = location.latitude,
                longitude = location.longitude,
                addressToShortString = currentPlace?.name ?: "",
                addressToString = currentPlace?.address ?: ""
            )
        } else {
            Timber.d("*TestLocation: currentPlace null")
            viewModel.getAddress(location)
        }
        stopLocation()
    }

    private fun validateGpsEnable() {
        val manager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        manager?.let {
            if (it.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(requireContext())) {
                Timber.d("Gps already enabled")
                getLocation()
            }

            if (!hasGPSDevice(requireContext())) {
                Timber.d("Gps not Supported")
                Toast.makeText(context, "Gps not Supported", Toast.LENGTH_SHORT).show()
            }

            if (!it.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(requireContext())) {
                Timber.d("Gps not enabled")
                enableLocation()
            } else {
                Timber.d("Gps already enabled")
            }
        }
    }

    private fun hasGPSDevice(context: Context): Boolean {
        val mgr = context
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders
        return providers.contains(LocationManager.GPS_PROVIDER)
    }

    private fun enableLocation() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(requireContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(object : ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {}
                    override fun onConnectionSuspended(i: Int) {
                        googleApiClient?.connect()
                    }
                })
                .addOnConnectionFailedListener { connectionResult ->
                    Timber.d("Location error ${connectionResult.errorCode}")
                }.build()
            googleApiClient?.connect()
        }

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 10000
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback {
            val status: Status = it.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        startIntentSenderForResult(
                            status.resolution.intentSender,
                            REQUEST_LOCATION,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Timber.e(e)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult $requestCode")
        when (requestCode) {
            REQUEST_LOCATION -> when (resultCode) {
                Activity.RESULT_OK -> {
                    getLocation()
                }
                Activity.RESULT_CANCELED -> {
                    moveMapToPosition(LatLng(LAT, LNG), 0f)
                    Utils.generalDialog(
                        "Ubicación",
                        "No se ha podido encontrar tu ubicación. Por favor posicionala manualmente",
                        true,
                        childFragmentManager
                    ) {}
                }
            }
        }
    }

    private fun getLocation() {
        val request = LocationRequest()
        request.interval = 30000
        request.fastestInterval = 10000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val permission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    if (location != null) {
                        Timber.d("*TestLocation: getLocation ${location.latitude}, ${location.longitude}")
                        moveMapToPosition(LatLng(location.latitude, location.longitude))
                    } else {
                        Timber.d("*TestLocation: getLocation null")
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(request, locationCallback, null)
        }
    }

    private fun stopLocation() {
        Timber.d("*TestLocation: stopLocation")
        fusedLocationClient?.let { locationClient ->
            locationCallback?.let {
                Timber.d("*TestLocation: stop locationCallback")
                locationClient.removeLocationUpdates(it)
            }
        }
    }

    private fun initializeFusedLocationClient() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        val request = LocationRequest()
        request.interval = 30000
        request.fastestInterval = 10000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val permission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location ->
                    location?.let {
                        Timber.d("*TestLocation: initializeFusedLocationClient ${it.latitude}, ${it.longitude}")
                        moveMapToPosition(LatLng(it.latitude, it.longitude))
                    }
                }?.addOnFailureListener { error ->
                    Timber.e(error)
                }
        }
    }

    private fun moveMapToPosition(latLng: LatLng, zoom: Float = ZOOM) {
        Timber.d("*TestLocation: moveMapToPosition $latLng")
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
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
            snapReadyCallback = GoogleMap.SnapshotReadyCallback { bitmap ->
                onSnapshotReady(bitmap)
            }
            mapLoadedCallback = OnMapLoadedCallback { googleMap?.snapshot(snapReadyCallback) }
            googleMap?.setOnMapLoadedCallback(mapLoadedCallback)
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
    private fun onSnapshotReady(nullableBitmap: Bitmap?) {
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
                        fileName = file.name,
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
                    this.showToast(getString(R.string.text_error_map))
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
                    Timber.d("*TestLocation: onPlaceSelected $latLng")
                    moveMapToPosition(latLng)
                    searchView.showSearchView()
                }
            }.addOnFailureListener { exception: java.lang.Exception ->
                if (exception is ApiException) {
                    val apiException = exception as ApiException
                    val statusCode = apiException.statusCode
                    // Handle error with given status code.
                    Timber.e("Place not found: " + exception.message)
                    this.showToast(getString(R.string.text_site_not_found))
                }
            }
    }
    //endregion
}