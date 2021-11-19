package lastie_wangechian_facebooklogin.com;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FacebookAuthentication";
    //widgets and variables
    private ImageView img_ProfilePic;
    private TextView txt_UserName;
    private LoginButton btn_Login;
    //esssential
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //intiliazing firebase and facebook
        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        //mapping widgets from their layout
        img_ProfilePic = findViewById(R.id.profilePicture);
        txt_UserName = findViewById(R.id.userName);
        btn_Login = findViewById(R.id.login_button);
        btn_Login.setPermissions("emails","public_profile");

        //calling upon the callbackManager
        mCallbackManager = CallbackManager.Factory.create();
        btn_Login.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess" + loginResult);
                //create a method to access credentials access tokens
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });

        //method checking if login or logout
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            }
        };

        //accesstoken tracker
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null){
                    mAuth.signOut();
                }
            }
        };
    }

    //method to access tokens
    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookToken" + accessToken);
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(MainActivity.this
                , new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //check whether task was successful
                        if (task.isSuccessful()) {
                            Log.d(TAG, "sign in using credentials: successfully.");
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            //create method that check current status of user's authentication
                            updateUI(mUser);
                        } else {
                            Log.d(TAG, "sign in using credentials: failed");
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    //method update user's session
    private void updateUI(FirebaseUser mUser) {
        //if user is currently login then key-in profile and username
        if (mUser != null) {
            txt_UserName.setText(mUser.getDisplayName());
            if (mUser.getPhotoUrl() != null) {
                Picasso.get().load(mUser.getPhotoUrl()).into(img_ProfilePic);
            } else
                Picasso.get().load(R.drawable.ic_person).into(img_ProfilePic);
        } else {
            txt_UserName.setText(null);
            Picasso.get().load(R.drawable.ic_person).into(img_ProfilePic);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}