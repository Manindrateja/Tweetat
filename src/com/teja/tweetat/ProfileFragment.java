package com.teja.tweetat;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileFragment extends Fragment {
	Date yesterday;
	TextView prof_name,timetoTweet;
    SharedPreferences pref;
    Bitmap bitmap;
    ImageView prof_img,tweet,signout,post_tweet,fetchScreenName;
    EditText tweet_text;
    ProgressDialog progress;
    Dialog tDialog;
    String tweetText;
    ConfigurationBuilder builder;
    AccessToken accessToken;
    Twitter twitter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.profile_fragment, container, false);
        prof_name = (TextView)view.findViewById(R.id.prof_name);
        timetoTweet = (TextView)view.findViewById(R.id.time2tweettext);
        pref = getActivity().getPreferences(0);
        prof_img = (ImageView)view.findViewById(R.id.prof_image);
        fetchScreenName = (ImageView)view.findViewById(R.id.fetchScreenname);
        fetchScreenName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new GetFollowers().execute();	
			}
		});
        tweet = (ImageView)view.findViewById(R.id.tweet);
        signout = (ImageView)view.findViewById(R.id.signout);
        signout.setOnClickListener(new SignOut());
        tweet.setOnClickListener(new Tweet());
        new LoadProfile().execute();
        builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
        builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));
        accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET", ""));
        twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
        return view;
        }
	    private class GetFollowers extends AsyncTask<String, String, String> {
	    	String output = "";
	    	IDs followers;
	    	long[] followersIds;
	    	int days = 3;
	    	int[] Result = new int[96];
	    	String tweetTime = "";
	    	ResponseList<twitter4j.Status> followers_status = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(getActivity());
            progress.setMessage("Please wait ...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();
            for(int i=0;i<Result.length;i++)
            	Result[i] = 0;
       }
       protected String doInBackground(String... args) {
              try {
            	  long cursor = -1;
            	  followers = twitter.getFollowersIDs(pref.getString("SCREENNAME", "") /*43017766*/, cursor, 100);
            	  followersIds = followers.getIDs(); 
             	  yesterday = new Date(System.currentTimeMillis()-days*24*60*60*1000L);

             	  /*for(long follower : followersIds)
             		 if(followers_status == null)
          	    		followers_status = twitter.getUserTimeline(follower);
          	    	else
          	    		followers_status.addAll(twitter.getUserTimeline(follower));*/
         		  for(int i=0;i<followersIds.length;i++)
         			 if(followers_status == null)
          	    		followers_status = twitter.getUserTimeline(followersIds[i]);
          	    	else
          	    		followers_status.addAll(twitter.getUserTimeline(followersIds[i]));
             			
             		
		        } catch (Exception e) {
		          // TODO Auto-generated catch block
		          e.printStackTrace();
		        }
           return null;
       }
           protected void onPostExecute(String res) {
        	  progress.dismiss();
        	  int a= 0;
        	  int maxindex = 0,max = 0,sum = 0;
        	  //taking tweets from last 3 days to analyze 
        	  for(twitter4j.Status status : followers_status)
       			if(status.getCreatedAt().compareTo(yesterday)>0){
       				output += status.getCreatedAt()+" "+((status.getCreatedAt().getTime()-yesterday.getTime())/(15*60*1000L))%Result.length+"\n";
       				Result[(int) (((status.getCreatedAt().getTime()-yesterday.getTime())/(15*60*1000L))%Result.length)]++;
       				a++;
       			}
//        	  System.out.println("total status: "+a);
        	  for(int i=0;i<Result.length;i++){
        	//	  System.out.println(i+" "+Result[i]);
        		  if(max<Result[i]){
        			  max = Result[i];
        			  maxindex = i;
        		  }
        		  sum +=Result[i];
        	  }
        	  //System.out.println("Actual Output:\n"+output);
        	  //System.out.println(followers_status.size()+"   "+sum);
        	  Date d= new Date();
        	  Calendar calendar = Calendar.getInstance(); 
        	  calendar.setTime(d); 
        	  int yyyy = calendar.get(Calendar.YEAR); 
        	  int mm = calendar.get(Calendar.MONTH); 
        	  int dd = calendar.get(Calendar.DATE);
        	  calendar.set(yyyy,mm,dd,00,00,00);
        	  //System.out.println(max+"   "+maxindex+" "+((System.currentTimeMillis()-calendar.getTimeInMillis())/(15*60*1000L))%Result.length);
        	  maxindex = maxindex + (int)((System.currentTimeMillis()-calendar.getTimeInMillis())/(15*60*1000L))%Result.length;
        	  maxindex %=Result.length;
        	  if(maxindex!=0)
        		  maxindex-=1;
        	  tweetTime = maxindex/4+":"+maxindex%4*15+"-"+(maxindex+1)/4+":"+(maxindex+1)%4*15;
        	  timetoTweet.setVisibility(View.VISIBLE);
        	  timetoTweet.setText(tweetTime);
              Toast.makeText(getActivity(), tweetTime, Toast.LENGTH_LONG).show();
           }
       }
    
	private class SignOut implements OnClickListener {
	    @Override
	    public void onClick(View arg0) {
	      // TODO Auto-generated method stub
	      SharedPreferences.Editor edit = pref.edit();
	            edit.putString("ACCESS_TOKEN", "");
	            edit.putString("ACCESS_TOKEN_SECRET", "");
	            edit.commit();
	            Fragment login = new LoginFragment();
	            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
	            ft.replace(R.id.content_frame, login);
	            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	            ft.addToBackStack(null);
	            ft.commit();
	    }
	    }
	    private class Tweet implements OnClickListener {
	    @Override
	    public void onClick(View v) {
	      // TODO Auto-generated method stub
	      tDialog = new Dialog(getActivity());
	      tDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	      tDialog.setContentView(R.layout.tweet_dialog);
	      tweet_text = (EditText)tDialog.findViewById(R.id.tweet_text);
	      post_tweet = (ImageView)tDialog.findViewById(R.id.post_tweet);
	      post_tweet.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	          // TODO Auto-generated method stub
	        new PostTweet().execute();
	        }
	      });
	      tDialog.show();
	    }}
	    private class PostTweet extends AsyncTask<String, String, String> {
	        @Override
	            protected void onPreExecute() {
	                super.onPreExecute();
	                progress = new ProgressDialog(getActivity());
	                progress.setMessage("Posting tweet ...");
	                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	                progress.setIndeterminate(true);
	                tweetText = tweet_text.getText().toString();
	                progress.show();
	        }
	           protected String doInBackground(String... args) {
	              try {
	          twitter4j.Status response = twitter.updateStatus(tweetText);
	          return response.toString();
	        } catch (TwitterException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        }
	           return null;
	           }
	           protected void onPostExecute(String res) {
	             if(res != null){
	               progress.dismiss();

	              Toast.makeText(getActivity(), "Tweet Sucessfully Posted", Toast.LENGTH_SHORT).show();
	               tDialog.dismiss();
	             }else{
	               progress.dismiss();
	                   Toast.makeText(getActivity(), "Error while tweeting !", Toast.LENGTH_SHORT).show();
	                   tDialog.dismiss();
	             }
	           }
	       }
	    private class LoadProfile extends AsyncTask<String, String, Bitmap> {
	        @Override
	            protected void onPreExecute() {
	                super.onPreExecute();
	                progress = new ProgressDialog(getActivity());
	                progress.setMessage("Loading Profile ...");
	                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	                progress.setIndeterminate(true);
	                progress.show();
	        }
	           protected Bitmap doInBackground(String... args) {
	             try {
	                   bitmap = BitmapFactory.decodeStream((InputStream)new URL(pref.getString("IMAGE_URL", "")).getContent());
	            } catch (Exception e) {
	                  e.printStackTrace();
	            }
	          return bitmap;
	           }
	           protected void onPostExecute(Bitmap image) {
	             Bitmap image_circle = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	             BitmapShader shader = new BitmapShader (bitmap,  TileMode.CLAMP, TileMode.CLAMP);
	             Paint paint = new Paint();
	             paint.setShader(shader);
	             Canvas c = new Canvas(image_circle);
	             c.drawCircle(image.getWidth()/2, image.getHeight()/2, image.getWidth()/2, paint);
	               prof_img.setImageBitmap(image_circle);
	               prof_name.setText("Welcome " +pref.getString("NAME", ""));
	               progress.hide();
	           }
	       }
}
