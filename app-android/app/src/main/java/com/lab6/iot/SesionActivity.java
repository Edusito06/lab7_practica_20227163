package com.lab6.iot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Pantalla 3: agregar (sin EXTRA_ID) o editar (con EXTRA_ID) una sesion de estudio. */
public class SesionActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "sesion_id";

    private CollectionReference col;
    private String sesionId; // null = nueva sesion
    private final Calendar fecha = Calendar.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private TextInputLayout tilCurso, tilFecha, tilHoras;
    private TextInputEditText etCurso, etFecha, etHoras;
    private RadioGroup rgTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion);

        col = FirestoreRefs.sesionesDelUsuario();
        if (col == null) {
            finish();
            return;
        }
        sesionId = getIntent().getStringExtra(EXTRA_ID);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(sesionId == null ? "Nueva sesión" : "Editar sesión");
        toolbar.setNavigationOnClickListener(v -> finish());

        tilCurso = findViewById(R.id.tilCurso);
        tilFecha = findViewById(R.id.tilFecha);
        tilHoras = findViewById(R.id.tilHoras);
        etCurso = findViewById(R.id.etCurso);
        etFecha = findViewById(R.id.etFecha);
        etHoras = findViewById(R.id.etHoras);
        rgTipo = findViewById(R.id.rgTipo);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Fecha por defecto: hoy
        mostrarFecha();
        etFecha.setOnClickListener(v -> elegirFecha());

        if (sesionId != null) {
            btnGuardar.setText("Guardar cambios");
            cargarSesion();
        }
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void elegirFecha() {
        new DatePickerDialog(this, (view, y, m, d) -> {
            fecha.set(y, m, d, 12, 0, 0); // mediodia para evitar problemas de zona horaria
            fecha.set(Calendar.MILLISECOND, 0);
            mostrarFecha();
        }, fecha.get(Calendar.YEAR), fecha.get(Calendar.MONTH), fecha.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarFecha() {
        etFecha.setText(fmt.format(fecha.getTime()));
    }

    /** Modo edicion: carga los datos actuales de Firestore */
    private void cargarSesion() {
        col.document(sesionId).get()
                .addOnSuccessListener(doc -> {
                    SesionEstudio s = doc.toObject(SesionEstudio.class);
                    if (s == null) {
                        Toast.makeText(this, "La sesión ya no existe", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    etCurso.setText(s.curso);
                    if (s.fecha != null) fecha.setTime(s.fecha);
                    mostrarFecha();
                    etHoras.setText(SesionEstudio.formatoHoras(s.horasDedicadas));
                    marcarTipo(s.tipoActividad);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void marcarTipo(String tipo) {
        if ("Teoría".equals(tipo)) rgTipo.check(R.id.rbTeoria);
        else if ("Práctica".equals(tipo)) rgTipo.check(R.id.rbPractica);
        else if ("Laboratorio".equals(tipo)) rgTipo.check(R.id.rbLaboratorio);
    }

    private void guardar() {
        tilCurso.setError(null);
        tilHoras.setError(null);

        String curso = etCurso.getText() == null ? "" : etCurso.getText().toString().trim();
        String horasTxt = etHoras.getText() == null ? "" : etHoras.getText().toString().trim().replace(',', '.');

        if (TextUtils.isEmpty(curso)) {
            tilCurso.setError("Ingresa el curso");
            return;
        }
        double horas;
        try {
            horas = Double.parseDouble(horasTxt);
        } catch (NumberFormatException e) {
            tilHoras.setError("Ingresa un número válido");
            return;
        }
        if (horas <= 0 || horas > 24) {
            tilHoras.setError("Debe estar entre 0 y 24 horas");
            return;
        }
        int checked = rgTipo.getCheckedRadioButtonId();
        if (checked == -1) {
            Toast.makeText(this, "Selecciona el tipo de actividad", Toast.LENGTH_SHORT).show();
            return;
        }
        String tipo = ((RadioButton) findViewById(checked)).getText().toString();

        SesionEstudio s = new SesionEstudio(curso, fecha.getTime(), horas, tipo);

        // Firestore actualiza la cache local al instante, asi que la lista
        // se refresca aunque cerremos la pantalla antes de la confirmacion del servidor.
        if (sesionId == null) {
            col.add(s).addOnFailureListener(this::mostrarError);
            Toast.makeText(this, "Sesión registrada", Toast.LENGTH_SHORT).show();
        } else {
            col.document(sesionId).set(s).addOnFailureListener(this::mostrarError);
            Toast.makeText(this, "Sesión actualizada", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void mostrarError(Exception e) {
        Toast.makeText(getApplicationContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
