package com.lab6.iot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/** Un solo lugar para la ruta de la coleccion del usuario actual. */
public final class FirestoreRefs {
    private FirestoreRefs() {
    }

    /** usuarios/{uid}/sesiones  (null si no hay sesion iniciada) */
    public static CollectionReference sesionesDelUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        return FirebaseFirestore.getInstance()
                .collection("usuarios").document(user.getUid())
                .collection("sesiones");
    }
}
