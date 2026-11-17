package com.lab6.iot;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lab6.iot.api.ApiClient;
import com.lab6.iot.api.Resumen;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Pantalla 4: graficos.
 * - Barras: horas de estudio por mes
 * - Torta: proporcion de horas por tipo de actividad
 * Ambos se filtran por curso.
 */
public class ChartsActivity extends AppCompatActivity {

    private static final String TODOS = "Todos los cursos";

    private final List<SesionEstudio> datos = new ArrayList<>();
    private final List<String> opcionesCurso = new ArrayList<>();
    private ArrayAdapter<String> spAdapter;

    private BarChart barChart;
    private PieChart pieChart;
    private Spinner spCurso;
    private ListenerRegistration listener;
    private CollectionReference col;

    // Lab 7
    private EditText etMeta;
    private TextView tvResumen;
    private List<SesionEstudio> filtradasActuales = new ArrayList<>();

    private final SimpleDateFormat claveMes = new SimpleDateFormat("yyyy-MM", Locale.US);
    private final SimpleDateFormat etiquetaMes = new SimpleDateFormat("MMM yyyy", new Locale("es", "PE"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        col = FirestoreRefs.sesionesDelUsuario();
        if (col == null) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        spCurso = findViewById(R.id.spCurso);

        configurarGraficos();

        // Lab 7: resumen calculado por los microservicios
        etMeta = findViewById(R.id.etMeta);
        tvResumen = findViewById(R.id.tvResumen);
        findViewById(R.id.btnCalcular).setOnClickListener(v -> consultarResumen());

        opcionesCurso.add(TODOS);
        spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcionesCurso);
        spCurso.setAdapter(spAdapter);
        spCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                dibujar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (col == null) return;
        listener = col.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snapshot == null) return;
            datos.clear();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                SesionEstudio s = doc.toObject(SesionEstudio.class);
                if (s != null) datos.add(s);
            }
            actualizarCursos();
            dibujar();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    /** Llena el filtro con los cursos que tiene el usuario, manteniendo la seleccion */
    private void actualizarCursos() {
        String seleccionado = (String) spCurso.getSelectedItem();
        TreeSet<String> cursos = new TreeSet<>();
        for (SesionEstudio s : datos) if (s.curso != null) cursos.add(s.curso);

        opcionesCurso.clear();
        opcionesCurso.add(TODOS);
        opcionesCurso.addAll(cursos);
        spAdapter.notifyDataSetChanged();

        int pos = seleccionado == null ? 0 : opcionesCurso.indexOf(seleccionado);
        spCurso.setSelection(Math.max(pos, 0));
    }

    private void configurarGraficos() {
        barChart.getDescription().setEnabled(false);
        barChart.setNoDataText("No hay sesiones para mostrar");
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getLegend().setEnabled(false);

        pieChart.getDescription().setEnabled(false);
        pieChart.setNoDataText("No hay sesiones para mostrar");
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(0xFFFFFFFF);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterText("Horas por\ntipo");
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
    }

    private void dibujar() {
        String filtro = (String) spCurso.getSelectedItem();
        if (filtro == null) filtro = TODOS;

        List<SesionEstudio> filtradas = new ArrayList<>();
        for (SesionEstudio s : datos) {
            if (TODOS.equals(filtro) || filtro.equals(s.curso)) filtradas.add(s);
        }

        filtradasActuales = filtradas;
        dibujarBarras(filtradas);
        dibujarTorta(filtradas);
    }

    /**
     * Lab 7: manda las horas del curso filtrado a servicio-estadisticas (Retrofit).
     * Este, a su vez, llama a servicio-horas con Feign usando Eureka.
     */
    private void consultarResumen() {
        double meta;
        try {
            meta = Double.parseDouble(etMeta.getText().toString().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            etMeta.setError("Meta no válida");
            return;
        }

        // "2.0,1.5,3.0"
        StringBuilder csv = new StringBuilder();
        for (SesionEstudio s : filtradasActuales) {
            if (csv.length() > 0) csv.append(',');
            csv.append(s.horasDedicadas);
        }

        tvResumen.setText("Consultando microservicios…");
        ApiClient.estadisticas().resumen(csv.toString(), meta).enqueue(new Callback<Resumen>() {
            @Override
            public void onResponse(@NonNull Call<Resumen> call, @NonNull Response<Resumen> resp) {
                Resumen r = resp.body();
                if (!resp.isSuccessful() || r == null) {
                    tvResumen.setText("Error HTTP " + resp.code());
                    return;
                }
                if (!r.disponible) { // respondio el fallback del circuit breaker
                    tvResumen.setText("⚠ " + r.mensaje);
                    return;
                }
                tvResumen.setText(
                        "Sesiones: " + r.sesiones +
                        "\nTotal: " + SesionEstudio.formatoHoras(r.totalHoras) + " h (" + (long) r.totalMinutos + " min)" +
                        "\nPromedio por sesión: " + SesionEstudio.formatoHoras(r.promedio) + " h" +
                        "\nTe faltan " + SesionEstudio.formatoHoras(r.faltante) + " h para tu meta de " +
                        SesionEstudio.formatoHoras(r.meta) + " h" +
                        "\nAtendido por servicio-horas en puertos: " + r.atendidoPor);
            }

            @Override
            public void onFailure(@NonNull Call<Resumen> call, @NonNull Throwable t) {
                // No llega a servicio-estadisticas (apagado, IP mala, sin cleartext...)
                tvResumen.setText("No se pudo conectar con servicio-estadisticas (" + ApiClient.BASE_URL + "): "
                        + t.getMessage());
            }
        });
    }

    /** Grafico de barras: horas de estudio por mes */
    private void dibujarBarras(List<SesionEstudio> sesiones) {
        // TreeMap con clave "yyyy-MM" -> queda ordenado cronologicamente
        TreeMap<String, Double> horasPorMes = new TreeMap<>();
        Map<String, String> etiquetas = new TreeMap<>();
        for (SesionEstudio s : sesiones) {
            if (s.fecha == null) continue;
            String clave = claveMes.format(s.fecha);
            Double acumulado = horasPorMes.get(clave);
            horasPorMes.put(clave, (acumulado == null ? 0 : acumulado) + s.horasDedicadas);
            etiquetas.put(clave, etiquetaMes.format(s.fecha));
        }

        if (horasPorMes.isEmpty()) {
            barChart.clear();
            return;
        }

        List<BarEntry> barras = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> e : horasPorMes.entrySet()) {
            barras.add(new BarEntry(i++, e.getValue().floatValue()));
            labels.add(etiquetas.get(e.getKey()));
        }

        BarDataSet set = new BarDataSet(barras, "Horas");
        set.setColor(0xFF1E4FA3);
        set.setValueTextSize(12f);
        BarData data = new BarData(set);
        data.setBarWidth(0.6f);

        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.setFitBars(true);
        barChart.animateY(500);
        barChart.invalidate();
    }

    /** Grafico de torta: proporcion de horas por tipo de actividad */
    private void dibujarTorta(List<SesionEstudio> sesiones) {
        Map<String, Double> horasPorTipo = new LinkedHashMap<>();
        for (String tipo : SesionEstudio.TIPOS) horasPorTipo.put(tipo, 0.0);
        for (SesionEstudio s : sesiones) {
            if (horasPorTipo.containsKey(s.tipoActividad)) {
                horasPorTipo.put(s.tipoActividad, horasPorTipo.get(s.tipoActividad) + s.horasDedicadas);
            }
        }

        List<PieEntry> entradas = new ArrayList<>();
        List<Integer> colores = new ArrayList<>();
        for (Map.Entry<String, Double> e : horasPorTipo.entrySet()) {
            if (e.getValue() > 0) {
                entradas.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
                colores.add(SesionEstudio.colorGrafico(e.getKey()));
            }
        }

        if (entradas.isEmpty()) {
            pieChart.clear();
            return;
        }

        PieDataSet set = new PieDataSet(entradas, "");
        set.setColors(colores);
        set.setSliceSpace(2f);
        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(13f);
        data.setValueTextColor(0xFFFFFFFF);

        pieChart.setData(data);
        pieChart.animateY(500);
        pieChart.invalidate();
    }
}
