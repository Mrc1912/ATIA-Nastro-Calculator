package it.atia.nastrocalculator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
  static final int BLACK=Color.BLACK, PANEL=Color.rgb(20,20,20), ORANGE=Color.rgb(255,100,0);
  EditText motor, reduction, diameter, length, target, rollerOut, speedOut, timeOut, ratioOut;
  BeltView belt; SharedPreferences prefs; boolean internal;

  @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("atia_values",MODE_PRIVATE);setContentView(ui());watch();calculate(0,null);}
  @Override protected void onPause(){super.onPause();prefs.edit().putString("motor",s(motor)).putString("reduction",s(reduction)).putString("diameter",s(diameter)).putString("length",s(length)).putString("target",s(target)).apply();}

  View ui(){
    ScrollView sv=new ScrollView(this);sv.setFillViewport(true);sv.setBackgroundColor(BLACK);
    LinearLayout root=column();root.setPadding(dp(18),dp(18),dp(18),dp(28));sv.addView(root);
    ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.atia_logo);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);root.addView(logo,new LinearLayout.LayoutParams(-1,dp(112)));
    TextView title=txt("NASTRO CALCULATOR",22,Color.WHITE,true);title.setGravity(17);title.setLetterSpacing(.1f);root.addView(title);
    TextView sub=txt("SIMULAZIONE E CALCOLO IN TEMPO REALE",11,ORANGE,true);sub.setGravity(17);sub.setLetterSpacing(.08f);sub.setPadding(0,dp(4),0,dp(14));root.addView(sub);
    belt=new BeltView();root.addView(belt,new LinearLayout.LayoutParams(-1,dp(170)));
    root.addView(section("PARAMETRI MODIFICABILI"));
    motor=field(root,"GIRI MOTORE","rpm","1400","motor");
    reduction=field(root,"RAPPORTO DI RIDUZIONE","1 :","30","reduction");
    diameter=field(root,"DIAMETRO RULLO","mm","80","diameter");
    length=field(root,"LUNGHEZZA NASTRO","mm","1000","length");
    target=field(root,"VELOCITÀ DESIDERATA","m/min","","target");
    Button button=new Button(this);button.setText("RICAVA IL RAPPORTO DALLA VELOCITÀ");button.setTextColor(BLACK);button.setTextSize(12);button.setTypeface(Typeface.DEFAULT,1);button.setBackground(box(ORANGE,ORANGE,12));button.setOnClickListener(v->requiredRatio());
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(50));bp.setMargins(0,dp(4),0,dp(16));root.addView(button,bp);
    root.addView(section("RISULTATI LIVE"));
    LinearLayout r1=row();rollerOut=result(r1,"GIRI RULLO (rpm)","");speedOut=result(r1,"VELOCITÀ (m/min)","");root.addView(r1);
    LinearLayout r2=row();timeOut=result(r2,"PERCORRENZA (s)","");ratioOut=result(r2,"RAPPORTO (1 : x)","");root.addView(r2);
    TextView note=txt("I risultati si aggiornano automaticamente mentre modifichi i parametri.",12,Color.LTGRAY,false);note.setGravity(17);note.setPadding(dp(8),dp(12),dp(8),dp(22));root.addView(note);
    TextView credits=txt("CREDITI\nATIA FOOD DEVICE\nwww.atiafooddevice.com",12,Color.WHITE,true);credits.setGravity(17);credits.setLineSpacing(dp(3),1);credits.setBackground(box(PANEL,ORANGE,12));credits.setPadding(dp(12),dp(15),dp(12),dp(15));credits.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.atiafooddevice.com"))));root.addView(credits);
    return sv;
  }

  EditText field(LinearLayout root,String label,String unit,String fallback,String key){
    LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(14),dp(7),dp(10),dp(7));c.setBackground(box(PANEL,Color.rgb(55,55,55),12));
    TextView l=txt(label,12,Color.WHITE,true);l.setGravity(Gravity.CENTER_VERTICAL);c.addView(l,new LinearLayout.LayoutParams(0,dp(48),1));
    EditText e=new EditText(this);e.setText(prefs.getString(key,fallback));e.setSelectAllOnFocus(true);e.setSingleLine();e.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);e.setTextColor(Color.WHITE);e.setTextSize(19);e.setBackgroundColor(Color.TRANSPARENT);e.setInputType(2|8192);c.addView(e,new LinearLayout.LayoutParams(dp(92),dp(48)));
    TextView u=txt(unit,11,ORANGE,true);u.setGravity(17);c.addView(u,new LinearLayout.LayoutParams(dp(58),dp(48)));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(64));p.setMargins(0,0,0,dp(8));root.addView(c,p);return e;
  }
  EditText result(LinearLayout row,String label,String initial){LinearLayout b=column();b.setGravity(17);b.setPadding(dp(6),dp(9),dp(6),dp(8));b.setBackground(box(PANEL,ORANGE,12));TextView l=txt(label,10,ORANGE,true);l.setGravity(17);b.addView(l);EditText v=new EditText(this);v.setText(initial);v.setTextSize(18);v.setTextColor(Color.WHITE);v.setGravity(17);v.setSingleLine();v.setSelectAllOnFocus(true);v.setInputType(2|8192);v.setBackgroundColor(Color.TRANSPARENT);b.addView(v,new LinearLayout.LayoutParams(-1,dp(47)));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(96),1);p.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(b,p);return v;}
  void watch(){watchField(motor,0);watchField(reduction,0);watchField(diameter,0);watchField(length,0);watchField(rollerOut,1);watchField(speedOut,2);watchField(target,2);watchField(timeOut,3);watchField(ratioOut,4);}
  void watchField(EditText field,int driver){field.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){if(!internal)calculate(driver,field);}public void afterTextChanged(Editable e){}});}
  void calculate(int driver,EditText source){try{double rpm=n(motor),d=n(diameter),len=n(length),red,roller,mMin,seconds;if(rpm<=0||d<=0||len<=0)throw new Exception();if(driver==1){roller=n(source);if(roller<=0)throw new Exception();red=rpm/roller;mMin=Math.PI*d/1000*roller;seconds=60*(len/1000)/mMin;}else if(driver==2){mMin=n(source);if(mMin<=0)throw new Exception();roller=mMin/(Math.PI*d/1000);red=rpm/roller;seconds=60*(len/1000)/mMin;}else if(driver==3){seconds=n(source);if(seconds<=0)throw new Exception();mMin=60*(len/1000)/seconds;roller=mMin/(Math.PI*d/1000);red=rpm/roller;}else if(driver==4){red=n(source);if(red<=0)throw new Exception();roller=rpm/red;mMin=Math.PI*d/1000*roller;seconds=60*(len/1000)/mMin;}else{red=n(reduction);if(red<=0)throw new Exception();roller=rpm/red;mMin=Math.PI*d/1000*roller;seconds=60*(len/1000)/mMin;}internal=true;reduction.setText(f("%.3f",red));rollerOut.setText(f("%.2f",roller));speedOut.setText(f("%.3f",mMin));timeOut.setText(f("%.3f",seconds));ratioOut.setText(f("%.3f",red));if(driver==2&&source==target)target.setText(f("%.3f",mMin));internal=false;belt.setPhysics((float)(len/1000),(float)(mMin/60));}catch(Exception e){internal=false;belt.setPhysics(0,0);}}
  void requiredRatio(){calculate(2,target);}
  class BeltView extends View{Paint p=new Paint(3);float beltOffset,progress,lengthM,speedMs;long last=System.nanoTime();BeltView(){super(MainActivity.this);setBackground(box(Color.rgb(8,8,8),ORANGE,14));}void setPhysics(float length,float speed){lengthM=length;speedMs=speed;last=System.nanoTime();invalidate();}
    protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),left=w*.1f,right=w*.9f,y=h*.6f,r=h*.16f,span=right-left;long now=System.nanoTime();float dt=Math.min((now-last)/1e9f,.05f);last=now;float px=(lengthM>0&&speedMs>0)?span*speedMs/lengthM:0;beltOffset=(beltOffset+dt*px)%42;if(lengthM>0&&speedMs>0)progress=(progress+dt*speedMs/lengthM)%1;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(5));p.setColor(Color.rgb(65,65,65));c.drawLine(left,y-r,right,y-r,p);c.drawLine(left,y+r,right,y+r,p);p.setStrokeWidth(dp(4));p.setColor(ORANGE);c.drawCircle(left,y,r,p);c.drawCircle(right,y,r,p);p.setStrokeWidth(dp(3));for(float x=left-r+((beltOffset+42)%42)-42;x<right+r;x+=42)c.drawLine(x,y-r-dp(5),x+dp(13),y-r+dp(5),p);p.setStyle(Paint.Style.FILL);float x=left+progress*span;c.drawRoundRect(x-dp(13),y-r-dp(23),x+dp(13),y-r,dp(4),dp(4),p);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(13));c.drawText(speedMs>0?f("FLUSSO  %.3f m/s",speedMs):"NASTRO FERMO",w/2,h*.2f,p);postInvalidateOnAnimation();}}
  TextView section(String s){TextView t=txt(s,12,ORANGE,true);t.setLetterSpacing(.08f);t.setPadding(dp(2),dp(12),0,dp(9));return t;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
  GradientDrawable box(int fill,int stroke,int radius){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(radius));g.setStroke(dp(1),stroke);return g;}
  TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,1);return t;}
  double n(EditText e){return Double.parseDouble(s(e).trim().replace(',','.'));}String s(EditText e){return e.getText().toString();}String f(String x,Object...v){return String.format(Locale.ITALY,x,v);}int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
