package com.example.myfirebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.myfirebaseapp.image.ImageClassificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PhotoActivity extends AppCompatActivity implements AlgoListener {

    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);


        ArrayList<Algo> arrayList = new ArrayList<>();
        /*
        arrayList.add(new Algo(R.drawable.baseline_image_black_48, "Image Classification", ImageClassificationActivity.class));
        */

        AlgoAdapter algoAdapter = new AlgoAdapter(arrayList, this);
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setAdapter(algoAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));



        authProfile = FirebaseAuth.getInstance();

        Intent intent = new Intent(PhotoActivity.this, ImageClassificationActivity.class);
        startActivity(intent);


    }

    //CREATING ACTION BAR MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if(id == R.id.menu_foto){
            Intent intent = new Intent(PhotoActivity.this, PhotoActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_view_profile){
            Intent intent = new Intent(PhotoActivity.this, UserProfileActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_update_profile){
            Intent intent = new Intent(PhotoActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_update_email){
            Intent intent = new Intent(PhotoActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_settings){
            Toast.makeText(this, "SETTINGS", Toast.LENGTH_SHORT).show();
        }else if(id == R.id.menu_change_password){
            Intent intent = new Intent(PhotoActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(PhotoActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(PhotoActivity.this, "Deslogado", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PhotoActivity.this, MainActivity.class);
            startActivity(intent);

            //Clear stack to prevent user coming back to UserProfileActivity pressing back button after Loggin out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //CLOSE UserProfileActivity

        }else{
            Toast.makeText(PhotoActivity.this, "Algo de errado aconteceu!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAlgoSelected(Algo algo) {
        Intent intent = new Intent(this, algo.activityClazz);
        intent.putExtra("name", algo.algoText);
        startActivity(intent);
    }
}

class AlgoAdapter extends RecyclerView.Adapter<AlgoViewHolder> {

    private List<Algo> algoList;
    private AlgoListener algoListener;

    public AlgoAdapter(List<Algo> algoList, AlgoListener listener) {
        this.algoList = algoList;
        this.algoListener = listener;
    }

    @NonNull
    @Override
    public AlgoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icons, parent, false);
        return new AlgoViewHolder(view, algoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlgoViewHolder holder, int position) {
        holder.bind(algoList.get(position));
    }

    @Override
    public int getItemCount() {
        return algoList.size();
    }
}

class AlgoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView iconImageView;
    private TextView algoTextView;
    private AlgoListener algoListener;
    private Algo algo;

    public AlgoViewHolder(@NonNull View itemView, AlgoListener algoListener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.algoListener = algoListener;

        iconImageView = itemView.findViewById(R.id.iconImageView);
        algoTextView = itemView.findViewById(R.id.algoTextView);

    }

    public void bind(Algo algo) {
        this.algo = algo;
        iconImageView.setImageResource(algo.iconResourceId);
        algoTextView.setText(algo.algoText);
    }

    @Override
    public void onClick(View v) {
        if (algoListener != null) {
            algoListener.onAlgoSelected(algo);
        }
    }
}

class Algo<T extends ImageClassificationActivity> {
    public int iconResourceId = R.drawable.ic_launcher_foreground;
    public String algoText = "";
    public Class<T> activityClazz;

    public Algo(int iconResourceId, String algoText, Class<T> activityClazz) {
        this.iconResourceId = iconResourceId;
        this.algoText = algoText;
        this.activityClazz = activityClazz;
    }
}

interface AlgoListener {
    void onAlgoSelected(Algo algo);
}