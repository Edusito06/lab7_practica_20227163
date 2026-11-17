package com.lab6.iot;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.VH> {

    public interface Listener {
        void onEditar(SesionEstudio s);

        void onEliminar(SesionEstudio s);
    }

    private final List<SesionEstudio> items;
    private final Listener listener;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public SesionAdapter(List<SesionEstudio> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sesion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SesionEstudio s = items.get(position);
        h.tvCurso.setText(s.curso);
        String fecha = s.fecha != null ? fmt.format(s.fecha) : "-";
        h.tvDetalle.setText(fecha + " · " + SesionEstudio.formatoHoras(s.horasDedicadas) + " h");
        h.tvTipo.setText(s.tipoActividad);
        h.tvTipo.setBackgroundTintList(ColorStateList.valueOf(SesionEstudio.colorFondo(s.tipoActividad)));
        h.tvTipo.setTextColor(SesionEstudio.colorTexto(s.tipoActividad));
        h.btnEditar.setOnClickListener(v -> listener.onEditar(s));
        h.btnEliminar.setOnClickListener(v -> listener.onEliminar(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCurso, tvDetalle, tvTipo;
        View btnEditar, btnEliminar;

        VH(View v) {
            super(v);
            tvCurso = v.findViewById(R.id.tvCurso);
            tvDetalle = v.findViewById(R.id.tvDetalle);
            tvTipo = v.findViewById(R.id.tvTipo);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnEliminar = v.findViewById(R.id.btnEliminar);
        }
    }
}
