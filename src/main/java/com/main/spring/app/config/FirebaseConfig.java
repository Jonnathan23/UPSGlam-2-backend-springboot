package com.main.spring.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import java.io.File;

@Configuration
public class FirebaseConfig {

    // 1. Bean para inicializar la aplicación de Firebase
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        Resource resource;
        
        // Intentar primero desde el sistema de archivos (volumen montado en Docker)
        File fileSystemPath = new File("/app/envs/serviceAccountKey.json");
        if (fileSystemPath.exists() && fileSystemPath.isFile()) {
            resource = new FileSystemResource(fileSystemPath);
        } else {
            // Fallback: buscar en el classpath (dentro del JAR)
            resource = new ClassPathResource("envs/serviceAccountKey.json");
        }

        // Construir opciones con las credenciales
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                // Aquí puedes configurar DatabaseUrl si usaras Realtime Database
                .build();

        // Si ya está inicializada (por si se llama dos veces), la obtenemos.
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        // Spring inyecta automáticamente el FirebaseApp del bean anterior
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    public Firestore firestoreDb(FirebaseApp firebaseApp) {
        // Spring inyecta el FirebaseApp y luego inicializamos el cliente de Firestore.
        return FirestoreClient.getFirestore(firebaseApp);
    }

}
