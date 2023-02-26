package com.trdz.mapper.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trdz.mapper.R
import com.trdz.mapper.databinding.FragmentWindowMapsMainBinding
import com.trdz.mapper.utility.REQUEST_CODE
import com.trdz.mapper.utility.hideKeyboard
import java.util.*

class WindowMaps: Fragment() {

	//region Elements
	private var _bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null
	private val bottomSheetBehavior get() = _bottomSheetBehavior!!
	private var _binding: FragmentWindowMapsMainBinding? = null
	private val binding get() = _binding!!
	private lateinit var map: GoogleMap
	private lateinit var current: Marker

	private val markers: MutableList<Marker> = mutableListOf()

	//endregion

	//region Base realization

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentWindowMapsMainBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
		mapFragment?.getMapAsync(callback)
		buttonBinds()
	}

	private val callback = OnMapReadyCallback { googleMap ->
		/**
		 * Manipulates the map once available.
		 * This callback is triggered when the map is ready to be used.
		 * This is where we can add markers or lines, add listeners or move the camera.
		 * In this case, we just add a marker near Sydney, Australia.
		 * If Google Play services is not installed on the device, the user will be prompted to
		 * install it inside the SupportMapFragment. This method will only be triggered once the
		 * user has installed Google Play services and returned to the app.
		 */
		map = googleMap
		map.run {
			setOnMapLongClickListener { setMarker(it) }
			setOnMarkerClickListener { toDetails(it);true }
			setOnMapClickListener {
				bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
		}
	}

	private fun toDetails(marker: Marker) {
		current = marker
		binding.mapper.popupSheet.name.setText(current.title)
		bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
	}

	private fun setLoad(state: Boolean = false) {
		if (state) binding.mapper.loadingLayout.visibility = View.VISIBLE
		else binding.mapper.loadingLayout.visibility = View.GONE
	}

	//endregion

	//region Geolocation segment

	private fun location() {
		checkPermission()
	}

	private fun checkPermission() {
		when {
			ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
				getLocation()
			}
			shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
				explain()
			}
			else -> {
				permissionGranting()
			}
		}
	}

	private fun explain() = AlertDialog.Builder(requireContext())
		.setTitle(getString(R.string.t_permission_title))
		.setMessage(getString(R.string.t_permission_explain))
		.setPositiveButton(getString(R.string.t_permission_yes)) { _, _ -> permissionGranting() }
		.setNegativeButton(getString(R.string.t_permission_no)) { dialog, _ -> setLoad(); dialog.dismiss() }
		.create()
		.show()

	private fun permissionGranting() {
		requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		if (requestCode == REQUEST_CODE) {
			for (i in permissions.indices) {
				if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					getLocation()
				}
				else {
					explain()
				}
			}
		}
		else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		}
	}

	@SuppressLint("MissingPermission")
	private fun getLocation() {
		context?.let {
			val locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				val providerGPS = locationManager.getProvider(LocationManager.GPS_PROVIDER)
				providerGPS?.let {
					locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						0,
						100f,
						locationListenerDistance
					)
				}
			}
		}
	}

	private val locationListenerDistance = object: LocationListener {
		override fun onLocationChanged(location: Location) {
			Log.d("@@@", "Loc $location")
			getAddressByLocation(location)
		}

		override fun onProviderDisabled(provider: String) {
			super.onProviderDisabled(provider)
		}

		override fun onProviderEnabled(provider: String) {
			super.onProviderEnabled(provider)
		}

	}

	fun getAddressByLocation(location: Location) {
		val geocoder = Geocoder(requireContext(), Locale.getDefault())
		Thread {
			val addressText = geocoder.getFromLocation(location.latitude, location.longitude, 1000000)[0].locality
			requireActivity().runOnUiThread {
				showAddressDialog(addressText, location)
			}
		}.start()
	}

	private fun showAddressDialog(address: String, location: Location) {
		activity?.let {
			AlertDialog.Builder(it)
				.setTitle(getString(R.string.t_location_success))
				.setMessage(address)
				.setPositiveButton(getString(R.string.t_open_details)) { _, _ -> goMe(address, location) }
				.setNegativeButton(getString(R.string.t_cancel_locations)) { dialog, _ -> setLoad(); dialog.dismiss() }
				.create()
				.show()
		}
	}

	private fun goMe(address: String, location: Location) {
		val locate = LatLng(
			location.latitude,
			location.longitude
		)
		setLoad()
		setMarker(locate, address)
		map.moveCamera(CameraUpdateFactory.newLatLng(locate))
	}

	//endregion

	//region Main functional

	private fun buttonBinds() {
		with(binding) {
			_bottomSheetBehavior = BottomSheetBehavior.from(mapper.popupSheet.bottomSheetContainer)
			bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
			bottomSheetBehavior.isHideable = true
			bottomSheetBehavior.addBottomSheetCallback(object:
				BottomSheetBehavior.BottomSheetCallback() {
				override fun onStateChanged(bottomSheet: View, newState: Int) {
					when (newState) {
						BottomSheetBehavior.STATE_DRAGGING -> {
						}
						BottomSheetBehavior.STATE_COLLAPSED -> {
							bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
						}
						BottomSheetBehavior.STATE_EXPANDED -> {
						}
						BottomSheetBehavior.STATE_HALF_EXPANDED -> {
						}
						BottomSheetBehavior.STATE_HIDDEN -> {
						}
						BottomSheetBehavior.STATE_SETTLING -> {
						}
					}
				}

				override fun onSlide(bottomSheet: View, slideOffset: Float) {}

			})
			mapper.popupSheet.bOk.setOnClickListener { hideKeyboard() }
			mapper.popupSheet.bDel.setOnClickListener { deleteMarker() }
			mapper.popupSheet.bSave.setOnClickListener { saveMarker() }
			bSearch.setOnClickListener { findLocation() }
			bMe.setOnClickListener { setLoad(true); location() }
		}
	}

	private fun deleteMarker() {
		current.remove()
		bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
	}

	private fun saveMarker() {
		//Были проблемы с рефрешем поҝтому метка пересоздаетсә
		setMarker(LatLng(current.position.latitude, current.position.longitude), binding.mapper.popupSheet.name.text.toString())
		deleteMarker()
	}

	private fun findLocation() {
		val locName: String = binding.searchAddress.text.toString()
		val geocoder = Geocoder(requireContext())
		val result = geocoder.getFromLocationName(locName, 1)
		if (result.isNotEmpty()) {
			val location = LatLng(
				result[0].latitude,
				result[0].longitude
			)
			setMarker(location)
			map.moveCamera(CameraUpdateFactory.newLatLng(location))
		}
	}

	private fun setMarker(location: LatLng, search: String = "") {
		markers.add(map.addMarker(MarkerOptions().position(location).title(search))!!)
	}

	//endregion

	companion object {
		@JvmStatic
		fun newInstance() = WindowMaps()
	}

}