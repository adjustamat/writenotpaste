package github.adjustamat.writenotpaste;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.urbandroid.sleep.captcha.CaptchaSupport;
import com.urbandroid.sleep.captcha.CaptchaSupportFactory;

public class WriteActivity extends AppCompatActivity implements TextWatcher,
                                                                OnFocusChangeListener,
                                                                OnTouchListener
{
   //private static final String DBG = "WriteActivity";
   private TextView textToCopy;
   private EditText writingTextBox;
   private CaptchaSupport support;
   private final CountDownTimer timer = new CountDownTimer(2, 2)
   {
      public void onTick(long millisUntilFinished){
      }
      
      public void onFinish(){
         clearClipboard();
      }
   };
   
   @SuppressLint("ClickableViewAccessibility")
   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.act_write);
      textToCopy = findViewById(R.id.textToCopy);
      writingTextBox = findViewById(R.id.writingTextBox);
      writingTextBox.setOnFocusChangeListener(this);
      writingTextBox.setOnTouchListener(this);
      writingTextBox.addTextChangedListener(this);
      support = CaptchaSupportFactory.create(this);
   }
   
   protected void onNewIntent(Intent intent){
      super.onNewIntent(intent);
      support = CaptchaSupportFactory.create(this, intent);
   }
   
   public void clearClipboard(){
      ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(
       ClipData.newPlainText(getText(R.string.app_name), getText(R.string.app_name)));
   }
   
   @Override
   protected void onResume(){
      super.onResume();
      
      textToCopy.setText("Text from intent"); // TODO: trim spaces and double-spaces.
      
      timer.start();
   }
   
   @SuppressLint("ClickableViewAccessibility")
   @Override
   protected void onPostResume(){
      super.onPostResume();
      //Log.v(DBG, "on post resume");
      writingTextBox.setOnTouchListener(this);
      writingTextBox.setOnFocusChangeListener(this);
   }
   
   @Override
   public void onFocusChange(View view, boolean b){
      timer.start();
   }
   
   @Override
   public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2){
   }
   
   @Override
   public void onTextChanged(CharSequence sequence, int i, int i1, int i2){
   }
   
   @Override
   public void afterTextChanged(Editable editable){
      String s = textToCopy.getText().toString();
      if(s.length() < 1) return;
      if(writingTextBox.getText().toString().equalsIgnoreCase(s)){
         Toast.makeText(this, R.string.toastSuccess, Toast.LENGTH_LONG).show();
         finish();
      }
   }
   
   @SuppressLint("ClickableViewAccessibility")
   @Override
   public boolean onTouch(View view, MotionEvent event){
      clearClipboard();
      return false;
   }
   
   @Override
   protected void onDestroy(){
      super.onDestroy();
      support.destroy();
   }
}