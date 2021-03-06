package layout;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.a.packthat.Friend;
import com.example.a.packthat.FriendsListAdapter;
import com.example.a.packthat.R;
import com.example.a.packthat.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class FriendsFragment extends Fragment {
    //instance variables
    private ArrayList<Friend> friendsList;
    public static FriendsListAdapter friendsListAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    //newInstance constructor for creating the fragment with arguments
    public static FriendsFragment newInstance(ArrayList<Friend> friendsList) {
        FriendsFragment friendsFragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putSerializable("friendsList", friendsList);
        friendsFragment.setArguments(args);
        return friendsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsList = (ArrayList<Friend>) getArguments().getSerializable("friendsList");
    }

    @Override
    public void onStart(){
        super.onStart();

        Button addFriendButton = (Button)getView().findViewById(R.id.button_display_add_friend_popup);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View addFriendLayout = inflater.inflate(R.layout.dialog_add_friend, null);
                final EditText addFriendEmail = (EditText) addFriendLayout.findViewById(R.id.editText_add_friend_email);
                final Button addFriend = (Button) addFriendLayout.findViewById(R.id.button_add_friend);

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Add Friend");
                alert.setView(addFriendLayout);
                alert.setCancelable(true);

                final AlertDialog dialog = alert.create();
                addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newFriendEmail = addFriendEmail.getText().toString();
                        if(isValidEmail(newFriendEmail)){
                            addFriend(newFriendEmail);
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        ListView friendsListView = (ListView)getView().findViewById(R.id.listView_friends);
        friendsListAdapter = new FriendsListAdapter(getContext(), R.id.listView_friends, friendsList);
        friendsListView.setAdapter(friendsListAdapter);
        //viewFriend dialog
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Friend friend = friendsList.get(position);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View viewFriendLayout = inflater.inflate(R.layout.dialog_view_friend, null);
                final Button deleteFriend = (Button) viewFriendLayout.findViewById(R.id.button_remove_friend);
                TextView friendName = (TextView) viewFriendLayout.findViewById(R.id.textView_view_friend_name);
                friendName.setText(friend.friendName);
                TextView friendEmail = (TextView) viewFriendLayout.findViewById(R.id.textView_view_friend_email);
                friendEmail.setText(friend.email);
                NetworkImageView friendProfImage = (NetworkImageView) viewFriendLayout.findViewById(R.id.imageView_view_friend_profile);
                RequestQueue requestQueue;
                ImageLoader imageLoader;
                requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
                imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache(){
                    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(10);
                    public void putBitmap(String url, Bitmap bitmap){
                        cache.put(url, bitmap);
                    }
                    public Bitmap getBitmap(String url){
                        return cache.get(url);
                    }
                });
                friendProfImage.setImageUrl("http://webdev.cs.uwosh.edu/students/thomaa04/PackThatLiveServer/uploads/" + friend.profileImg, imageLoader);

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setView(viewFriendLayout);
                alert.setCancelable(true);

                final AlertDialog dialog = alert.create();
                deleteFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
                            JSONObject params = new JSONObject();
                            params.put("userId", User.Id);
                            params.put("friendId", friend.friendId);
                            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/PackThatLiveServer/deleteFriend.php";
                            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try{
                                                int affected = response.getInt("affected");
                                                if(affected >= 1){
                                                    Toast.makeText(getContext().getApplicationContext(), "Successfully deleted.", Toast.LENGTH_SHORT).show();
                                                    friendsList.remove(friend);
                                                    friendsListAdapter.notifyDataSetChanged();
                                                    dialog.dismiss();
                                                }
                                            }catch (Exception e){
                                                Toast.makeText(getContext().getApplicationContext(), "Error deleting friend", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.i("FriendsFragment", error.getStackTrace().toString());
                                            System.out.println("Error");
                                        }
                                    });
                            requestQueue.add(jsObjRequest);
                        }catch (Exception e){
                            Log.i("FriendsFragment", e.getStackTrace().toString());
                            Toast.makeText(getContext().getApplicationContext(), "Error deleting friend.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        if (!email.equals("")) {
            if (email.matches("^([A-Z|a-z|0-9](\\.|_){0,1})+[A-Z|a-z|0-9]\\@([A-Z|a-z|0-9])+((\\.){0,1}[A-Z|a-z|0-9]){2}\\.[a-z]{2,3}$")) {
                return true;
            }else{
                Toast.makeText(getContext().getApplicationContext(), "Email must be in a proper emailing format (e.g: email@email.com)", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext().getApplicationContext(), "You need to add a friend's email.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void addFriend(String friendEmail){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            JSONObject params = new JSONObject();
            params.put("email", friendEmail);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/PackThatLiveServer/selectFriend.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if(response != null){
                                    int friendId = response.getInt("Id");
                                    String friendName = response.getString("Name");
                                    String friendEmail = response.getString("Email");
                                    String friendImg = response.getString("ProfileImg");
                                    Friend newFriend = new Friend(friendId, friendName, friendEmail,friendImg);
                                    if(friendId != 0){
                                        if(friendId != User.Id){
                                            if (!hasFriend(newFriend.friendId)) {
                                                addFriendToDatabase(newFriend);
                                            }else{
                                                Toast.makeText(getContext().getApplicationContext(), "This person is already your friend!", Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            Toast.makeText(getContext().getApplicationContext(), "Don't make yourself your own friend, find other people.", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(getContext().getApplicationContext(), "No user has that email address.", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(getContext().getApplicationContext(), "No user has that email address.", Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception e){
                                Toast.makeText(getContext().getApplicationContext(), "Error finding friend", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", Arrays.toString(error.getStackTrace()));
                            System.out.println("Error");
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.getStackTrace().toString());
            Toast.makeText(getContext().getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean hasFriend(int id){
        for (Friend friend: friendsList) {
            if(friend.friendId == id){
                return true;
            }
        }
        return false;
    }

    public void addFriendToDatabase(final Friend newFriend){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            JSONObject params = new JSONObject();
            params.put("userId", User.Id);
            params.put("friendId", newFriend.friendId);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/PackThatLiveServer/addFriend.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if(response != null){
                                    int affected = response.getInt("affected");
                                    if(affected == 1){
                                        friendsList.add(newFriend);
                                        friendsListAdapter.notifyDataSetChanged();
                                    }
                                }
                            }catch (Exception e){
                                Toast.makeText(getContext().getApplicationContext(), "Error finding friend", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.getStackTrace().toString());
                            System.out.println("Error");
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.getStackTrace().toString());
            Toast.makeText(getContext().getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }
}
