package com.example.frontend.controller.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.frontend.R
import com.example.frontend.controller.io.ServiceImpl
import com.example.frontend.controller.models.Reserva
import androidx.lifecycle.Observer
import com.example.frontend.controller.models.Persona
import com.example.frontend.controller.models.Zone
import com.example.frontend.controller.util.PreferenceHelper
import com.example.frontend.controller.util.PreferenceHelper.set
import com.example.frontend.controller.util.ReservaAdapter
import com.opencanarias.pruebasync.util.AppDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.activity_reserva_detallada.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ListActivity : AppCompatActivity() {
    private val preferences by lazy {
        PreferenceHelper.defaultPrefs(this)
    }

    override fun onBackPressed(){
        super.onBackPressed();
        val intent = Intent(this, ZoneActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private lateinit var state: String

    private lateinit var reserva: ArrayList<Reserva>
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ReservaAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    @RequiresApi(Build.VERSION_CODES.N)
    private val selectedCalendar: Calendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.N)
    private val selectedCalendar2: Calendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        reserva = ArrayList<Reserva>()

        viewManager = LinearLayoutManager(this)
        viewAdapter = ReservaAdapter(reserva, this)
        recyclerView = findViewById<RecyclerView>(R.id.locationViewPager2)
        // use a linear layout manager
        recyclerView.layoutManager = viewManager
        // specify an viewAdapter (see also next example)
        recyclerView.adapter = viewAdapter

        recyclerView.itemAnimator

        state = this.intent.getStringExtra("state").toString()
        val zoneId = this.intent.getIntExtra("zoneId", 1)
        preferences["zoneIII"] = zoneId

        //getZone(zoneId)
        val timeZone = "GMT+1"
        val prueba = "1";

        var listaZones = emptyList<Zone>()

        val database = AppDatabase.getDatabase(this)

        var getReservas = emptyList<Reserva>()
        database.reservas().getByDate(zoneId,obtenerFechaActual(timeZone).toString()).observe(this, Observer {
            getReservas = it
            viewAdapter = ReservaAdapter(getReservas as ArrayList<Reserva>, this)
            recyclerView.adapter = viewAdapter
        })

        database.zonas().getById(zoneId).observe(this, Observer{
            listaZones = it
            val url = "https://cryptic-dawn-95434.herokuapp.com/img/"
            val imageUrl = url + listaZones[0].url_img + ".jpg"
            Picasso.with(this).load(imageUrl).into(bg_lists);
        })

        listeners(zoneId)
    }

    private fun listeners(zoneId: Int) {
        button2.setOnClickListener {
            var getReservas = emptyList<Reserva>()
            val localizador = localizadorReserva.text.toString()
            val database = AppDatabase.getDatabase(this)
            database.reservas().getByLocalizador(localizador).observe(this, Observer{
                getReservas = it

                viewAdapter = ReservaAdapter(getReservas as ArrayList<Reserva>, this)
                recyclerView.adapter = viewAdapter
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickSheduleDate(v: View?) {

        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
            //Toast.makeText(this, "$y-$m-$d", Toast.LENGTH_SHORT).show()
            selectedCalendar.set(y, m, d)
            reservationDate.setText(
                resources.getString(
                    R.string.date_format,
                    y,
                    (m + 1).twoDigits(),
                    d.twoDigits()
                )
            )
            val opeIdPref = preferences.getInt("zoneIII", 0)
            var getReservas = emptyList<Reserva>()
            val database = AppDatabase.getDatabase(this)
            database.reservas().getByDate(opeIdPref ,resources.getString(R.string.date_format, y, (m + 1).twoDigits(), d.twoDigits())).observe(this, Observer{
                getReservas = it

                viewAdapter = ReservaAdapter(getReservas as ArrayList<Reserva>, this)

                recyclerView.adapter = viewAdapter
            })
        }

        // new dialog
        val datePickerDialog =
            DatePickerDialog(this, R.style.datepicker, listener, year, month, dayOfMonth)
        val datePicker = datePickerDialog.datePicker

        // show dialog
        datePickerDialog.show()
    }

    private fun Int.twoDigits() = if (this >= 10) this.toString() else "0$this"

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    fun obtenerFechaConFormato(formato: String?, zonaHoraria: String?): String? {
        val calendar = Calendar.getInstance()
        val date = calendar.time
        val sdf: SimpleDateFormat
        sdf = SimpleDateFormat(formato)
        sdf.setTimeZone(TimeZone.getTimeZone(zonaHoraria))
        return sdf.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun obtenerFechaActual(zonaHoraria: String?): String? {
        val formato = "yyyy-MM-dd"
        return obtenerFechaConFormato(formato, zonaHoraria)
    }
}
/*
private fun getBookingsDate(zoneId: Int, prueba: String) {
    val roomServiceImpl = ServiceImpl()
    roomServiceImpl.getBookingByDate(this, zoneId, prueba) { response ->
        run {
            if (response != null) {
                viewAdapter.notifyDataSetChanged()
                viewAdapter.reservaList = response
            }
            viewAdapter.notifyDataSetChanged()
        }
    }
}

private fun getBookingLocalizador(localizador_id: String) {
    val roomServiceImpl = ServiceImpl()
    roomServiceImpl.getBookingByLocalizador(this, localizador_id) { response ->
        run {
            if (response != null) {
                viewAdapter.notifyDataSetChanged()
                viewAdapter.reservaList = response
            }
            viewAdapter.notifyDataSetChanged()
        }
    }
}

private fun getZone(zoneId: Int) {
    val bicycleServiceImpl = ServiceImpl()
    bicycleServiceImpl.getZoneById(this, zoneId) { response ->
        run {
            val url = "http://192.168.56.1:8000/img/"
            val imageUrl = url + response?.url_img + ".jpg"
            Picasso.with(this).load(imageUrl).into(bg_lists);
        }
    }
}*/

