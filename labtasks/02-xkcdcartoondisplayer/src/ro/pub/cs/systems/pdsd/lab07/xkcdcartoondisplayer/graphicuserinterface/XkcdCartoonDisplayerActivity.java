package ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.graphicuserinterface;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.R;
import ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.entities.XkcdCartoonInfo;
import ro.pub.cs.systems.pdsd.xkcdcartoondisplayer.general.Constants;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class XkcdCartoonDisplayerActivity extends Activity {
	
	private TextView xkcdCartoonTitleTextView;
	private ImageView xkcdCartoonImageView;
	private TextView xkcdCartoonUrlTextView;
	private Button previousButton, nextButton;
	
	private class XkcdCartoonUrlButtonClickListener implements Button.OnClickListener {
		
		String xkcdComicUrl;
		
		public XkcdCartoonUrlButtonClickListener(String xkcdComicUrl) {
			this.xkcdComicUrl = xkcdComicUrl;
		}
		
		@Override
		public void onClick(View view) {
			new XkcdCartoonDisplayerAsyncTask().execute(xkcdComicUrl);
		}
	}
	
	private class ButtonOnClickListener implements View.OnClickListener {
		private String url;
		public ButtonOnClickListener(String url) {
			super();
			this.url = url;
		}
		@Override
		public void onClick(View v) {
			Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(this.url));
            startActivity(i);
        }
		
	}
	
	private class XkcdCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XkcdCartoonInfo> {

		@Override
		protected XkcdCartoonInfo doInBackground(String... urls) {
			
			XkcdCartoonInfo xkcdCartoonInfo = new XkcdCartoonInfo();
			
			// TODO: exercise 5a)
			// 1. obtain the content of the web page (whose Internet address is stored in urls[0])
			// - create an instance of a HttpClient object
			// - create an instance of a HttpGet object
			// - create an instance of a ResponseHandler object
			// - execute the request, thus obtaining the web page source code
			HttpEntity httpGetEntity = null;
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(urls[0]);
				HttpResponse httpGetResponse = null;
				httpGetResponse = httpClient.execute(httpGet);
				httpGetEntity = httpGetResponse.getEntity();

			} catch (Exception exception) {
				Log.e(Constants.TAG, exception.getMessage());
				if (Constants.DEBUG) {
					exception.printStackTrace();
				}
			}

			// 2. parse the web page source code
			// - cartoon title: get the tag whose id equals "ctitle"
			// - cartoon url
			//   * get the first tag whose id equals "comic"
			//   * get the embedded <img> tag
			//   * get the value of the attribute "src"
			//   * prepend the protocol: "http:"
			// - cartoon content: get the input stream attached to the url and decode it into a Bitmap
			// - previous cartoon address
			//   * get the first tag whole rel attribute equals "prev"
			//   * get the href attribute of the tag
			//   * prepend the value with the base url: http://www.xkcd.com
			//   * attach the previous button a click listener with the address attached
			// - next cartoon address
			//   * get the first tag whole rel attribute equals "next"
			//   * get the href attribute of the tag
			//   * prepend the value with the base url: http://www.xkcd.com
			//   * attach the next button a click listener with the address attached
			if (httpGetEntity != null) {
				String pageSourceCode;
				try {
					pageSourceCode = EntityUtils.toString(httpGetEntity);
					Document document = Jsoup.parse(pageSourceCode);
					Element htmlTag = document.child(0);
					Element divTagIdCtitle = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
					xkcdCartoonInfo.setCartoonTitle(divTagIdCtitle.ownText());
					
					Element divTagIdComic = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
					String cartoonInternetAddress = divTagIdComic.getElementsByTag(Constants.IMG_TAG).attr(Constants.SRC_ATTRIBUTE);
					xkcdCartoonInfo.setCartoonUrl("http:" + cartoonInternetAddress);
					
					Bitmap bitmap = BitmapFactory.decodeStream(httpGetEntity.getContent());
					xkcdCartoonInfo.setCartoonContent(bitmap);
					
					Element aTagRelPrev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
					String previousCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelPrev.attr(Constants.HREF_ATTRIBUTE);
					xkcdCartoonInfo.setPreviousCartoonUrl(previousCartoonInternetAddress);
					
					Element aTagRelNext = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
					String nextCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelNext.attr(Constants.HREF_ATTRIBUTE);
					xkcdCartoonInfo.setNextCartoonUrl(nextCartoonInternetAddress);
					previousButton = (Button)findViewById(R.id.previous_button);
					nextButton = (Button)findViewById(R.id.next_button);
					
					previousButton.setOnClickListener(new ButtonOnClickListener(previousCartoonInternetAddress));
					nextButton.setOnClickListener(new ButtonOnClickListener(nextCartoonInternetAddress));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			return xkcdCartoonInfo;

		}
		
		@Override
		protected void onPostExecute(XkcdCartoonInfo xkcdCartoonInfo) {
			
			// TODO: exercise 5b)
			// map each member of xkcdCartoonInfo object to the corresponding widget
			// cartoonTitle -> xkcdCartoonTitleTextView
			// cartoonContent -> xkcdCartoonImageView
			// cartoonUrl -> xkcdCartoonUrlTextView
			xkcdCartoonImageView = (ImageView)findViewById(R.id.xkcd_cartoon_image_view);
			xkcdCartoonTitleTextView = (TextView)findViewById(R.id.xkcd_cartoon_title_text_view);
			xkcdCartoonUrlTextView = (TextView)findViewById(R.id.xkcd_cartoon_url_text_view);
			
			xkcdCartoonImageView.setImageBitmap(xkcdCartoonInfo.getCartoonContent());
			xkcdCartoonTitleTextView.setText(xkcdCartoonInfo.getCartoonTitle());
			xkcdCartoonUrlTextView.setText(xkcdCartoonInfo.getCartoonUrl());
			/*XkcdCartoonDisplayerActivity.this.runOnUiThread(new Runnable() {
				public void run() {

				}
			});*/

		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xkcd_cartoon_displayer);
		
		xkcdCartoonTitleTextView = (TextView)findViewById(R.id.xkcd_cartoon_title_text_view);
		xkcdCartoonImageView = (ImageView)findViewById(R.id.xkcd_cartoon_image_view);
		xkcdCartoonUrlTextView = (TextView)findViewById(R.id.xkcd_cartoon_url_text_view);
		
		previousButton = (Button)findViewById(R.id.previous_button);
		nextButton = (Button)findViewById(R.id.next_button);
		
		new XkcdCartoonDisplayerAsyncTask().execute(Constants.XKCD_INTERNET_ADDRESS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.xkcd_cartoon_displayer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
