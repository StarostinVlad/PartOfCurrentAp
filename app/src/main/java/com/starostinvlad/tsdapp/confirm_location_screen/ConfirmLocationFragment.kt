package com.starostinvlad.tsdapp.confirm_location_screen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.databinding.FragmentConfirmLocationBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import javax.inject.Inject


class ConfirmLocationFragment : Fragment(R.layout.fragment_confirm_location),
    ConfirmLocationFragmentContract {

    var lastLocation: Location? = null

    private val binding: FragmentConfirmLocationBinding by viewBinding()

    @Inject
    lateinit var presenter: ConfirmLocationFragmentPresenter

    private var mapController: IMapController? = null
    var marker: Marker? = null

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        with(binding) {

            Configuration.getInstance()
                .load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))
            map.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            map.setTileSource(TileSourceFactory.MAPNIK)

            map.setMultiTouchControls(true)
            mapController = map.controller
            mapController?.setZoom(17.0)
            val startPoint = GeoPoint(55.72688359517398, 52.48236862851809)
            mapController?.setCenter(startPoint)

            btnConfirmLocation.setOnClickListener {
                presenter.onConfirmLocationBtnClick()
            }
            presenter.onLoaded()
        }
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val listener = LocationListener { location ->
            lastLocation = location
            val startPoint = GeoPoint(location.latitude, location.longitude)
            getPositionMarker().position = startPoint
        }
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            1000,
            5f,
            listener
        )
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, listener)

    }

    override fun showRow(points: MutableList<GeoPoint>) {
        Log.d("Fragment", "attachView: points:$points")
        val polygon = Polygon()
        polygon.fillPaint.color = Color.GREEN //set fill color
        polygon.outlinePaint.color = Color.GREEN
        polygon.points = points
        polygon.title = "A sample polygon"
        binding.map.overlays.add(polygon)
    }

    override fun initMap(geoPoint: GeoPoint) {
        mapController?.setZoom(18.5)
        mapController?.setCenter(geoPoint)
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun getLastLocation() {
        lastLocation?.let {
            presenter.onLocationRecv(
                it.latitude,
                it.longitude
            )
        }

    }


    private fun getPositionMarker(): Marker { //Singelton
        if (marker == null) {
            marker = Marker(binding.map)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.icon = ContextCompat.getDrawable(requireContext(), R.drawable.person)
            binding.map.overlays.add(marker)
        }
        return marker!!
    }

    override fun closeTask() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Внимание!")
            .setMessage("Задача завершена")
            .setCancelable(false)
            .setPositiveButton("ОК") { _, _ ->
                findNavController().navigate(R.id.operationsFragment, null, navOptions {
                    popUpTo(R.id.confirmLocationFragment) {
                        inclusive = true
                    }
                })
            }
        builder.show()


    }

    override fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoading(show: Boolean) {
        binding.progress.isVisible = show
    }

}