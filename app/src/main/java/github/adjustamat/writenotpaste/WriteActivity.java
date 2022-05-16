package github.adjustamat.writenotpaste;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.urbandroid.sleep.captcha.CaptchaSupport;
import com.urbandroid.sleep.captcha.CaptchaSupportFactory;

public class WriteActivity extends Activity implements TextWatcher,
                                                       OnFocusChangeListener,
                                                       OnTouchListener
{
   private static final String DBG = "WriteActivity";
   
   public static final Uri CONTENT_URI = Uri.parse("content://com.urbandroid.sleep.alarmclock/alarm");
   //public static final String NON_DEEPSLEEP_WAKEUP_WINDOWN = "ndswakeupwindow";
   //notif = cursor.getString(ALERT_INDEX_STR);
   public static final int MESSAGE_INDEX_STR = 0;
   public static final String MESSAGE = "message";
   public static final int DAYS_OF_WEEK_INDEX_INT = 1;
   public static final String DAYS_OF_WEEK = "daysofweek";
   public static final int HOUR_INDEX_INT = 2;
   public static final String HOUR = "hour";
   public static final int MINUTES_INDEX_INT = 3;
   public static final String MINUTES = "minutes";
   public static final int ALARM_TIME_INDEX_INT = 4;
   public static final String ALARM_TIME = "alarmtime";
   public static final int SUSPEND_TIME_INDEX_INT = 5;
   public static final String SUSPEND_TIME = "suspendtime";
   private static final String[] projection = new String[]{
    MESSAGE, DAYS_OF_WEEK, HOUR, MINUTES, ALARM_TIME, SUSPEND_TIME
   };
   private static final String whereSelection = "enabled = 1";
   public static final String SORT_ORDER = HOUR + ", " + MINUTES + " ASC";
   
   // private View timeoutView; // LATER
   private TextView textToCopy;
   private String textToCompareUC;
   private EditText writingTextBox;
   private CaptchaSupport support;
   
   private final CountDownTimer clipboardTimer = new CountDownTimer(2, 2)
   {
      public void onTick(long millisUntilFinished){
      }
      
      public void onFinish(){
         clearClipboard();
      }
   };
//   private final RemainingTimeListener remainingTimeListener = new RemainingTimeListener()
//   {
//      @Override
//      public void timeRemain(int seconds, int aliveTimeout){
//         // in oncreate: findViewById(R.id.timeout);
////         timeoutView.setText(
////          String.format(getString(R.string.time_remaining_text), seconds, aliveTimeout)
////         );
//      }
//   };
   
   
   // now: elapsed = 1503081287, System = 1650568347815
   // alarmtime=     1305258336, hour=22, minutes=25
   // mellan dem: 207300 sekunder, 3455 minuter, 57.583333333 timmar (exakt 09:35 + 48h)
   // alarmtime=     1512558336, hour=8, minutes=0
   
   // days=71 means mon,tue,wed,sun. days=103 means mon,tue,wed,sat,sun. days=127 means every day.
   
   /*
2022-04-21 21:24:28.177 31462-31462/github.adjustamat.writenotpaste E/WriteActivity: now: elapsed = 1503801649, System = 1650569068177
2022-04-21 21:24:28.177 31462-31462/github.adjustamat.writenotpaste E/WriteActivity: lägg fram kläder (HOUSE«
alarmtime=1305258336, hour=22, minutes=25
suspendtime=-1, days=127 »
2022-04-21 21:24:28.178 31462-31462/github.adjustamat.writenotpaste E/WriteActivity: vakna«
alarmtime=1426158336, hour=8, minutes=0
suspendtime=-1, days=103 »

// 52m,16.992s i framtiden = 3136992 ms
// now: urbandroid = 1302121344
// difference to System (1650569563008, which is from 1970): 1649267441664
// 2022 + 95 dagar och 17.8 timmar (alltså 2022-04-05 nån gång, om ett år är 365.25 dagar)

// 14.173128889 dagar sedan (innan 00:00 den 2022-04-21, alltså den 2022-04-06 nån gång.)

2022-04-21 21:32:43.008 3737-3737/github.adjustamat.writenotpaste E/WriteActivity: now: elapsed = 49874, System = 1650569563008
2022-04-21 21:32:43.008 3737-3737/github.adjustamat.writenotpaste E/WriteActivity: lägg fram kläder (HOUSE«
alarmtime=1305258336, hour=22, minutes=25
suspendtime=-1, days=127 »
2022-04-21 21:32:43.008 3737-3737/github.adjustamat.writenotpaste E/WriteActivity: vakna«
alarmtime=1426158336, hour=8, minutes=0
suspendtime=-1, days=103 means tue wed sat sun»


2022-04-21 22:36:03.063 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: now: elapsed = 3849929, System = 1650573363063
2022-04-21 22:36:03.063 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: vakna«
alarmtime=1515258336, hour=8, minutes=0
suspendtime=-1, days=71 »
2022-04-21 22:36:03.063 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: lägg fram kläder (HOUSE«
alarmtime=1910718336, hour=22, minutes=36
suspendtime=-1, days=8 »

2022-04-21 22:39:27.371 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: now: elapsed = 4054237, System = 1650573567371
2022-04-21 22:39:27.372 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: vakna«
alarmtime=1515258336, hour=8, minutes=0
suspendtime=-1, days=71 »
2022-04-21 22:39:27.372 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: lägg fram kläder (HOUSE«
alarmtime=1910718336, hour=22, minutes=36
suspendtime=-1, days=8 »

2022-04-21 22:41:42.605 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: now: elapsed = 4189471, System = 1650573702605
2022-04-21 22:41:42.605 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: vakna«
alarmtime=1515258336, hour=8, minutes=0
suspendtime=-1, days=71 »
2022-04-21 22:41:42.605 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: lägg fram kläder (HOUSE«
alarmtime=1910718336, hour=22, minutes=36
suspendtime=-1, days=8 »

2022-04-21 22:41:59.113 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: now: elapsed = 4205979, System = 1650573719113
2022-04-21 22:41:59.113 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: vakna«
alarmtime=1515258336, hour=8, minutes=0
suspendtime=-1, days=71 »
2022-04-21 22:41:59.113 10281-10281/github.adjustamat.writenotpaste E/WriteActivity: lägg fram kläder (HOUSE«
alarmtime=1910718336, hour=22, minutes=36
suspendtime=-1, days=8 »


    */
   // mon = 1
   // tue = 2
   // wed = 4
   // thu = 8
   // fri = 16
   // sat = 32
   // sun = 64
   private static int[] todays = {0, 64, 1, 2, 4, 8, 16, 32};
   private static int[] yesterdays = {0, 32, 64, 1, 2, 4, 8, 16};
   
   private static class TimeInfo implements Comparable<TimeInfo>
   {
      final int weekdays, hour, minutes;
      final String message;
      final Calendar now;
      
      public TimeInfo(int weekdays, int hour, int minutes, String message, Calendar now){
         this.weekdays = weekdays;
         this.hour = hour;
         this.minutes = minutes;
         this.message = message;
         this.now = now;
      }
      
      public boolean isTodayOrYesterday(){
         return (weekdays & todays[now.get(Calendar.DAY_OF_WEEK)]) > 0 ||
                (weekdays & yesterdays[now.get(Calendar.DAY_OF_WEEK)]) > 0;
      }
      
      private int getComparableInt(){
         int h = now.get(Calendar.HOUR_OF_DAY);
         int m = now.get(Calendar.MINUTE);
         
         if((weekdays & todays[now.get(Calendar.DAY_OF_WEEK)]) > 0){
            h = hour - h;
            m = minutes - m;
            // TODO
         }
         else if((weekdays & yesterdays[now.get(Calendar.DAY_OF_WEEK)]) > 0){
         // TODO: add 24 hours?
         }
         return h;
//         switch(now.get(Calendar.DAY_OF_WEEK)){
//         case Calendar.MONDAY://2 ->1
//            if((weekdays & 1) > 0){ // today
//
//            }
//            else if((weekdays & 64) > 0){// yesterday
//
//            }
//         case Calendar.TUESDAY://3 ->2
//            if((weekdays & 2) > 0){ // today
//
//            }
//            else if((weekdays & 1) > 0){ // yesterday
//
//            }
//         case Calendar.WEDNESDAY://4 ->4
//            if((weekdays & 4) > 0){ // today
//
//            }
//            else if((weekdays & 2) > 0){ // yesterday
//
//            }
//         case Calendar.THURSDAY://5 ->8
//            if((weekdays & 8) > 0){ // today
//
//            }
//            else if((weekdays & 4) > 0){ // yesterday
//
//            }
//         case Calendar.FRIDAY://6 ->16
//            if((weekdays & 16) > 0){ // today
//
//            }
//            else if((weekdays & 8) > 0){ // yesterday
//
//            }
//         case Calendar.SATURDAY://7 ->32
//            if((weekdays & 32) > 0){ // today
//
//            }
//            else if((weekdays & 16) > 0){ // yesterday
//
//            }
//         case Calendar.SUNDAY://1 ->64
//         default:
//            if((weekdays & 64) > 0){ // today
//
//            }
//            else if((weekdays & 32) > 0){ // yesterday
//
//            }
//         }
      }
      
      @Override
      public int compareTo(TimeInfo info){
         return getComparableInt() - info.getComparableInt();
      }
   }
   
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
      initialize(savedInstanceState, null);
   }
   
   private void initialize(Bundle savedInstanceState, Intent intent){
      try{
         support = CaptchaSupportFactory.create(this);
      }
      catch(RuntimeException exception){
         Log.e(DBG, "support is null because", exception);
         support = null;
      }
      if(support != null){
         if(support.isPreviewMode()){
            textToCompareUC = getString(R.string.preview_text_to_copy);
            textToCopy.setText(R.string.preview_text_to_copy);
            return;
         }
//         else if(support.isOperationalMode()){
//            support.setRemainingTimeListener(remainingTimeListener);
//         }
          /*
    When CaptchaSupport object is created you can
    get difficulty (1-5) : use CaptchaSupport.getDifficulty()
    call unsolved method when captcha was not solved but user left activity
    or use advanced features like CaptchaFinder for getting list of all available captchas on mobile and launch them via CaptchaLauncher
       */
      }
      ContentResolver resolver = getContentResolver();
      Cursor cursor = resolver.query(CONTENT_URI, projection, whereSelection, null, SORT_ORDER);
      
      String messageToCopy = getString(R.string.app_name);
      long alarmTime, suspendTime;
//      int daysOfWeek, hour, minutes;
//      Log.e(DBG,
//       "\n\nnow: elapsed = " + SystemClock.elapsedRealtime() + ", System = " + System.currentTimeMillis() + "\n"
//      );
      Calendar now = Calendar.getInstance();
      LinkedList<TimeInfo> list = new LinkedList<>();
      if(cursor != null){
         /*
          * Moves to the next row in the cursor. Before the first movement in the cursor, the
          * "row pointer" is -1, and if you try to retrieve data at that position you will get an
          * exception.
          */
         while(cursor.moveToNext()){
            alarmTime = cursor.getLong(ALARM_TIME_INDEX_INT);
            suspendTime = cursor.getLong(SUSPEND_TIME_INDEX_INT);
            
            list.add(new TimeInfo(cursor.getInt(DAYS_OF_WEEK_INDEX_INT),
             cursor.getInt(HOUR_INDEX_INT),
             cursor.getInt(MINUTES_INDEX_INT),
             cursor.getString(MESSAGE_INDEX_STR),
             now
            ));
            
            Log.i(DBG, "alarmtime=" + alarmTime + ", suspendtime=" + suspendTime);
         }
         cursor.close();
         Collections.sort(list);
         TimeInfo last = list.getLast();
         if(last.isTodayOrYesterday()){ // TODO: check suspendTime/alarmTime when preponing or when skip next.
            messageToCopy = last.message;
         }
         else{
            Log.e(DBG, "nothing today or yesterday");
         }
         
      }
      else{
         Log.e(DBG, "cursor is null!");
         Toast.makeText(this, "Error: could not get message from alarm!", Toast.LENGTH_LONG).show();
         if(support != null)
            support.solved(); // .solved() broadcasts an intent back to Sleep as Android to let it know that captcha is solved
         finish();
         return;
      }
      // trim and remove double-spaces and other unwanted whitespaces.
      messageToCopy = messageToCopy.trim().replaceAll("\\s+", " ");
      // textToCopy cannot be app_name or empty!
      if(messageToCopy.length() < 1 || messageToCopy.equalsIgnoreCase(getString(R.string.app_name)))
         messageToCopy = getString(R.string.preview_text_to_copy);
      textToCopy.setText(messageToCopy);
      textToCompareUC = messageToCopy;
      
   }
   
   protected void onNewIntent(Intent intent){
      super.onNewIntent(intent);
      initialize(null, intent);
   }
   
   public void clearClipboard(){
      ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(
       ClipData.newPlainText(getText(R.string.app_name), getText(R.string.app_name)));
   }
   
   @Override
   public void onBackPressed(){
      super.onBackPressed();
      if(support != null)
         support.unsolved(); // .unsolved() broadcasts an intent back to AlarmAlertFullScreen that captcha was not solved
   }
   
   @Override
   public void onUserInteraction(){
      super.onUserInteraction();
      if(support != null)
         support.alive(); // .alive() refreshes captcha timeout
      // - intended to be sent on user interaction primarily, but can be called anytime anywhere
   }
   
   @Override
   protected void onResume(){
      super.onResume();
      clipboardTimer.start();
   }
   
   @SuppressLint("ClickableViewAccessibility")
   @Override
   protected void onPostResume(){
      super.onPostResume();
      //Log.e(DBG, "on post resume");
      writingTextBox.setOnTouchListener(this);
      writingTextBox.setOnFocusChangeListener(this);
   }
   
   @Override
   public void onFocusChange(View view, boolean b){
      clipboardTimer.start();
   }
   
   @Override
   public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2){
   }
   
   @Override
   public void onTextChanged(CharSequence sequence, int i, int i1, int i2){
   }
   
   @Override
   public void afterTextChanged(Editable editable){
      // trim spaces and double-spaces. remember to check uppercase (and without :,.- osv.)
      if(writingTextBox.getText().toString().equalsIgnoreCase(textToCompareUC)){
         Toast.makeText(this, R.string.toastSuccess, Toast.LENGTH_LONG).show();
         if(support != null)
            support.solved(); // .solved() broadcasts an intent back to Sleep as Android to let it know that captcha is solved
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
      if(support != null)
         support.destroy();
      super.onDestroy();
   }
}