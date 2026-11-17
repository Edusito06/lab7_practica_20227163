package com.lab6.iot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/** Pantalla 2: lista de sesiones de estudio del usuario, en tiempo real. */
public class HomeActivity extends AppCompatActivity implements SesionAdapter.Listener {

    private CollectionReference col;
    private final List<SesionEstudio> lista = new ArrayList<>();
    private SesionAdapter adapter;
    private ListenerRegistration listener;
    private TextView tvContador, tvVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        col = FirestoreRefs.sesionesDelUsuario();
        if (col == null) { // sin sesion -> volver al login
            irAlLogin();
            return;
        }

        setContentView(R.layout.activity_home);

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> cerrarSesion());

        tvContador = findViewById(R.id.tvContador);
        tvVacio = findViewById(R.id.tvVacio);

        RecyclerView rv = findViewById(R.id.rvSesiones);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionAdapter(lista, this);
        rv.setAdapter(adapter);

        findViewById(R.id.fabAgregar).setOnClickListener(v ->
                startActivity(new Intent(this, SesionActivity.class)));
        findViewById(R.id.btnGraficos).setOnClickListener(v ->
                startActivity(new Intent(this, ChartsActivity.class)));
    }

    // Tiempo real: el listener se registra en onStart y se libera en onStop
    @Override
    protected void onStart() {
        super.onStart();
        if (col == null) return;
        listener = col.orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot == null) return;
                    lista.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        SesionEstudio s = doc.toObject(SesionEstudio.class);
                        if (s != null) lista.add(s);
                    }
                    adapter.notifyDataSetChanged();
                    int n = lista.size();
                    tvContador.setText(n == 1 ? "1 sesión registrada" : n + " sesiones registradas");
                    tvVacio.setVisibility(n == 0 ? View.VISIBLE : View.GONE);
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

    // EDITAR: abre la pantalla 3 con el id de la sesion
    @Override
    public void onEditar(SesionEstudio s) {
        Intent i = new Intent(this, SesionActivity.class);
        i.putExtra(SesionActivity.EXTRA_ID, s.id);
        startActivity(i);
    }

    // ELIMINAR con confirmacion
    @Override
    public void onEliminar(SesionEstudio s) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar sesión")
                .setMessage("¿Deseas eliminar la sesión de \"" + s.curso + "\"?")
                .setPositiveButton("Eliminar", (d, w) ->
                        col.document(s.id).delete()
                                .addOnSuccessListener(x ->
                                        Toast.makeText(this, "Sesión eliminada", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        // AuthUI.signOut cierra Firebase y tambien la cuenta de Google
        AuthUI.getInstance().signOut(this).addOnCompleteListener(t -> irAlLogin());
    }

    private void irAlLogin() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
