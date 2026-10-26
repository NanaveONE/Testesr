package com.skyrus.liveeditor;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Standalone editor for Android views INFLATED from SKYrus's actual layout XML resources. */
public final class MainActivity extends AppCompatActivity {
    private static final int COLOR_BG=Color.rgb(17,22,30), COLOR_TEXT=Color.WHITE;
    private static final int EXPORT=200, IMPORT=201;
    private static final String FORMAT="skyrus-source-layout-transforms", DOC="source_view_changes";
    private static final class Scene {
        final String key,title; final int layout; final String prefs;
        Scene(String key,String title,int layout,String prefs){this.key=key;this.title=title;this.layout=layout;this.prefs=prefs;}
    }
    private final List<Scene> scenes = new ArrayList<>();
    private final Map<View,float[]> original = new IdentityHashMap<>();
    private JSONObject changes = new JSONObject();
    private Scene current;
    private FrameLayout preview;
    private FrameLayout sceneHolder;
    private View sourceRoot, selected;
    private TouchOverlay overlay;
    private Spinner scenePicker;
    private LinearLayout editPanel;
    private TextView selectedName, scaleLabel, opacityLabel, offsetLabel, status;
    private SeekBar sizeSlider, alphaSlider;
    private Button modeButton;
    private boolean editing=true, changingWidgets=false, changingPicker=false;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(COLOR_BG);
        getWindow().setNavigationBarColor(COLOR_BG);
        buildScenes();
        try { changes=new JSONObject(getPreferences(MODE_PRIVATE).getString(DOC,"{}")); } catch(Exception ignored){}
        buildUI();
        showScene("home");
    }
    private void buildScenes(){
        scenes.add(new Scene("home","Biblioteca — main_activity.xml",R.layout.main_activity,null));
        scenes.add(new Scene("home_menu","Menu inicial — dialog_home_menu.xml",R.layout.dialog_home_menu,null));
        scenes.add(new Scene("home_search","Buscar — dialog_home_search.xml",R.layout.dialog_home_search,null));
        scenes.add(new Scene("item_grid","Cartão de jogo — app_item_grid.xml",R.layout.app_item_grid,null));
        scenes.add(new Scene("item_list","Linha de jogo — app_item_linear.xml",R.layout.app_item_linear,null));
        scenes.add(new Scene("settings_system","Configurações / Sistema",R.layout.settings_activity,"system"));
        scenes.add(new Scene("settings_graphics","Configurações / Gráficos",R.layout.settings_activity,"graphics"));
        scenes.add(new Scene("settings_audio","Configurações / Áudio",R.layout.settings_activity,"audio"));
        scenes.add(new Scene("settings_appearance","Configurações / Aparência",R.layout.settings_activity,"appearance"));
        scenes.add(new Scene("settings_debug","Configurações / Depuração",R.layout.settings_activity,"debug"));
        scenes.add(new Scene("settings_about","Configurações / Sobre",R.layout.settings_activity,"about"));
        scenes.add(new Scene("settings_content","Configurações / Conteúdo (XML)",R.layout.settings_activity,"content"));
        scenes.add(new Scene("settings_input","Configurações / Controles (XML)",R.layout.settings_activity,"input"));
        scenes.add(new Scene("settings_overlay","Configurações / Overlay (XML)",R.layout.settings_activity,"overlay"));
        scenes.add(new Scene("settings_frame","Configurações / Frame Generation",R.layout.settings_activity,"frame"));
        scenes.add(new Scene("controller","Lista de controles — controller_activity.xml",R.layout.controller_activity,null));
        scenes.add(new Scene("driver","Drivers — gpu_driver_activity.xml",R.layout.gpu_driver_activity,null));
        scenes.add(new Scene("onscreen","Controles virtuais — on_screen_edit_activity.xml",R.layout.on_screen_edit_activity,null));
        scenes.add(new Scene("overlay_editor","Editor de desempenho — activity_performance_overlay_editor.xml",R.layout.activity_performance_overlay_editor,null));
        scenes.add(new Scene("emulation","Tela emulação — emu_activity.xml",R.layout.emu_activity,null));
        scenes.add(new Scene("resource_app_dialog","Componente • app_dialog.xml",R.layout.app_dialog,null));
        scenes.add(new Scene("resource_app_item_grid_compact","Componente • app_item_grid_compact.xml",R.layout.app_item_grid_compact,null));
        scenes.add(new Scene("resource_button_dialog","Componente • button_dialog.xml",R.layout.button_dialog,null));
        scenes.add(new Scene("resource_controller_checkbox_item","Componente • controller_checkbox_item.xml",R.layout.controller_checkbox_item,null));
        scenes.add(new Scene("resource_controller_header","Componente • controller_header.xml",R.layout.controller_header,null));
        scenes.add(new Scene("resource_controller_item","Componente • controller_item.xml",R.layout.controller_item,null));
        scenes.add(new Scene("resource_game_info_row_developer","Componente • game_info_row_developer.xml",R.layout.game_info_row_developer,null));
        scenes.add(new Scene("resource_game_info_row_format","Componente • game_info_row_format.xml",R.layout.game_info_row_format,null));
        scenes.add(new Scene("resource_game_info_row_size","Componente • game_info_row_size.xml",R.layout.game_info_row_size,null));
        scenes.add(new Scene("resource_game_info_row_title_id","Componente • game_info_row_title_id.xml",R.layout.game_info_row_title_id,null));
        scenes.add(new Scene("resource_game_info_row_version","Componente • game_info_row_version.xml",R.layout.game_info_row_version,null));
        scenes.add(new Scene("resource_gamepad_reassign_dialog","Componente • gamepad_reassign_dialog.xml",R.layout.gamepad_reassign_dialog,null));
        scenes.add(new Scene("resource_gpu_driver_item","Componente • gpu_driver_item.xml",R.layout.gpu_driver_item,null));
        scenes.add(new Scene("resource_gpu_driver_settings_fragment","Componente • gpu_driver_settings_fragment.xml",R.layout.gpu_driver_settings_fragment,null));
        scenes.add(new Scene("resource_keyboard_dialog","Componente • keyboard_dialog.xml",R.layout.keyboard_dialog,null));
        scenes.add(new Scene("resource_license_dialog","Componente • license_dialog.xml",R.layout.license_dialog,null));
        scenes.add(new Scene("resource_loader_error_dialog","Componente • loader_error_dialog.xml",R.layout.loader_error_dialog,null));
        scenes.add(new Scene("resource_loader_error_item","Componente • loader_error_item.xml",R.layout.loader_error_item,null));
        scenes.add(new Scene("resource_on_screen_edit_mini_fab","Componente • on_screen_edit_mini_fab.xml",R.layout.on_screen_edit_mini_fab,null));
        scenes.add(new Scene("resource_osc_slider","Componente • osc_slider.xml",R.layout.osc_slider,null));
        scenes.add(new Scene("resource_pipeline_loading","Componente • pipeline_loading.xml",R.layout.pipeline_loading,null));
        scenes.add(new Scene("resource_preference_profile_picture","Componente • preference_profile_picture.xml",R.layout.preference_profile_picture,null));
        scenes.add(new Scene("resource_preference_widget_material_switch","Componente • preference_widget_material_switch.xml",R.layout.preference_widget_material_switch,null));
        scenes.add(new Scene("resource_progress_dialog","Componente • progress_dialog.xml",R.layout.progress_dialog,null));
        scenes.add(new Scene("resource_rumble_dialog","Componente • rumble_dialog.xml",R.layout.rumble_dialog,null));
        scenes.add(new Scene("resource_stick_dialog","Componente • stick_dialog.xml",R.layout.stick_dialog,null));
        scenes.add(new Scene("resource_titlebar","Componente • titlebar.xml",R.layout.titlebar,null));
        scenes.add(new Scene("resource_view_search_bar","Componente • view_search_bar.xml",R.layout.view_search_bar,null));
    }
    private int d(float v){return Math.round(getResources().getDisplayMetrics().density*v);}
    private TextView text(String s,int sp){TextView t=new TextView(this);t.setText(s);t.setTextColor(COLOR_TEXT);t.setTextSize(sp);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private Button btn(String label,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextSize(11);b.setOnClickListener(v->action.run());return b;}
    private void buildUI(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);root.setPadding(d(8),d(5),d(8),d(5));root.setBackgroundColor(COLOR_BG);
        setContentView(root);
        TextView heading=text("SKYrus • Editor da interface original",18);root.addView(heading,new LinearLayout.LayoutParams(-1,d(33)));
        TextView info=text("XML, imagens e estilos extraídos do ZIP do SKYrus",11);info.setTextColor(0xffb4c2d6);root.addView(info,new LinearLayout.LayoutParams(-1,d(23)));
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
        scenePicker=new Spinner(this);String[] labels=new String[scenes.size()];for(int i=0;i<scenes.size();i++) labels[i]=scenes.get(i).title;
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);scenePicker.setAdapter(adapter);
        bar.addView(scenePicker,new LinearLayout.LayoutParams(0,d(47),1));
        modeButton=btn("Prévia",this::toggleMode);bar.addView(modeButton,new LinearLayout.LayoutParams(d(85),d(47)));root.addView(bar);
        preview=new FrameLayout(this);preview.setBackgroundColor(0xff14181d);
        preview.setClipChildren(false); preview.setClipToPadding(false);
        LinearLayout.LayoutParams viewport=new LinearLayout.LayoutParams(-1,0,1);viewport.topMargin=d(4);viewport.bottomMargin=d(5);
        root.addView(preview,viewport);
        sceneHolder=new FrameLayout(this);preview.addView(sceneHolder,new FrameLayout.LayoutParams(-1,-1));
        overlay=new TouchOverlay();preview.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
        status=text("",11);status.setTextColor(0xffb4c2d6);root.addView(status,new LinearLayout.LayoutParams(-1,d(32)));
        editPanel=new LinearLayout(this);editPanel.setOrientation(LinearLayout.VERTICAL);
        selectedName=text("Toque em um elemento real para selecioná-lo",12);editPanel.addView(selectedName);
        LinearLayout controls=new LinearLayout(this);
        LinearLayout first=new LinearLayout(this);first.setOrientation(LinearLayout.VERTICAL);
        scaleLabel=text("Tamanho: 100%",11);first.addView(scaleLabel);
        sizeSlider=new SeekBar(this);sizeSlider.setMax(250);sizeSlider.setProgress(100);first.addView(sizeSlider);
        controls.addView(first,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout second=new LinearLayout(this);second.setOrientation(LinearLayout.VERTICAL);
        opacityLabel=text("Transparência: 100%",11);second.addView(opacityLabel);
        alphaSlider=new SeekBar(this);alphaSlider.setMax(100);alphaSlider.setProgress(100);second.addView(alphaSlider);
        controls.addView(second,new LinearLayout.LayoutParams(0,-2,1));editPanel.addView(controls);
        offsetLabel=text("Deslocamento: 0 × 0 dp",11);editPanel.addView(offsetLabel);
        LinearLayout editButtons=new LinearLayout(this);editButtons.addView(btn("Restaurar item",this::resetSelected),new LinearLayout.LayoutParams(0,d(41),1));
        editButtons.addView(btn("Restaurar tela",this::resetScreen),new LinearLayout.LayoutParams(0,d(41),1));
        editPanel.addView(editButtons);root.addView(editPanel);
        LinearLayout footer=new LinearLayout(this);footer.addView(btn("Salvar",this::save),new LinearLayout.LayoutParams(0,d(45),1));
        footer.addView(btn("Exportar",()->chooseFile(true)),new LinearLayout.LayoutParams(0,d(45),1));
        footer.addView(btn("Importar",()->chooseFile(false)),new LinearLayout.LayoutParams(0,d(45),1));root.addView(footer);
        scenePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> v){}
            public void onItemSelected(AdapterView<?> p,View v,int pos,long id){if(!changingPicker)showScene(scenes.get(pos).key);}
        });
        sizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar b){}
            public void onStopTrackingTouch(SeekBar b){save();}
            public void onProgressChanged(SeekBar b,int n,boolean fromUser){
                if(!fromUser||changingWidgets||selected==null)return;
                float[] orig=original.get(selected);if(orig==null)return;
                float k=Math.max(.35f,n/100f);selected.setScaleX(orig[2]*k);selected.setScaleY(orig[3]*k);
                scaleLabel.setText("Tamanho: "+n+"%");rememberSelected();overlay.invalidate();
            }
        });
        alphaSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar b){}
            public void onStopTrackingTouch(SeekBar b){save();}
            public void onProgressChanged(SeekBar b,int n,boolean fromUser){
                if(!fromUser||changingWidgets||selected==null)return;
                selected.setAlpha(n/100f);opacityLabel.setText("Transparência: "+n+"%");rememberSelected();overlay.invalidate();
            }
        });
    }
    private void toggleMode(){editing=!editing;overlay.setVisibility(editing?View.VISIBLE:View.GONE);
        editPanel.setVisibility(editing?View.VISIBLE:View.GONE);modeButton.setText(editing?"Prévia":"Editar");
        status.setText(editing?"Editar: arraste componentes reais, altere tamanho e opacidade":"Prévia: navegue pelo menu; dados do emulador são demonstrativos");
    }
    private void showScene(String key){
        Scene next=null;for(Scene s:scenes)if(s.key.equals(key)){next=s;break;}if(next==null)return;
        current=next;selected=null;original.clear();
        for(Fragment f:new ArrayList<>(getSupportFragmentManager().getFragments())){
            if(f instanceof PreviewPrefsFragment)getSupportFragmentManager().beginTransaction().remove(f).commitNowAllowingStateLoss();
        }
        sceneHolder.removeAllViews();
        try{
            if(current.layout==R.layout.view_search_bar){
                FrameLayout wrapper=new FrameLayout(this);
                LayoutInflater.from(this).inflate(current.layout,wrapper,true);
                sourceRoot=wrapper;
            } else sourceRoot=LayoutInflater.from(this).inflate(current.layout,sceneHolder,false);
            if(current.layout==R.layout.dialog_home_menu||current.layout==R.layout.dialog_home_search){
                FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);
                sceneHolder.addView(sourceRoot,lp);
            }else sceneHolder.addView(sourceRoot,new FrameLayout.LayoutParams(-1,-1));
            if(current.prefs!=null){
                PreviewPrefsFragment f=PreviewPrefsFragment.newInstance(current.prefs);
                getSupportFragmentManager().beginTransaction().replace(R.id.settings,f).commitNowAllowingStateLoss();
                hookSettingsTabs();
            }
            configureDynamicContent();
            captureOriginal(sourceRoot);
            applySavedTransforms(sourceRoot);
            setupPreviewClicks();
            sceneHolder.post(()->{if(current!=null) {captureOriginalMissing(sourceRoot);applySavedTransforms(sourceRoot);overlay.invalidate();}});
            status.setText("Original: "+current.title+" · toque para selecionar");
        }catch(Exception e){
            sceneHolder.removeAllViews();sourceRoot=null;
            TextView problem=text("Não foi possível carregar o layout real:\n"+e.getClass().getSimpleName()+": "+e.getMessage(),13);
            sceneHolder.addView(problem);status.setText("Falha ao inflar recurso original");
        }
        changingPicker=true;scenePicker.setSelection(scenes.indexOf(current));changingPicker=false;
        updateSliders();overlay.invalidate();
    }
    private void hookSettingsTabs(){
        String[] names={"system","graphics","audio","appearance","debug","about"};
        int[] ids={R.id.settings_tab_system,R.id.settings_tab_graphics,R.id.settings_tab_audio,R.id.settings_tab_appearance,R.id.settings_tab_debug,R.id.settings_tab_about};
        for(int i=0;i<ids.length;i++){final String scene="settings_"+names[i];View v=sourceRoot.findViewById(ids[i]);if(v!=null)v.setOnClickListener(w->showScene(scene));}
    }
    private void configureDynamicContent(){
        if(current.key.equals("home")){
            RecyclerView list=sourceRoot.findViewById(R.id.app_list);
            if(list!=null){list.setLayoutManager(new GridLayoutManager(this,2));list.setAdapter(new RecyclerView.Adapter<Holder>(){
                @NonNull public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type){
                    View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_grid,parent,false);return new Holder(v);
                }
                public void onBindViewHolder(@NonNull Holder h,int i){
                    TextView t=h.itemView.findViewById(R.id.text_title);if(t!=null)t.setText("Jogo demonstrativo "+(i+1));
                    TextView a=h.itemView.findViewById(R.id.text_author);if(a!=null)a.setText("Dados de exemplo, layout original");
                    ImageView icon=h.itemView.findViewById(R.id.icon);if(icon!=null)icon.setImageResource(R.drawable.skymu_icon_new);
                }
                public int getItemCount(){return 4;}
            });}
            View placeholder=sourceRoot.findViewById(R.id.rom_placeholder);if(placeholder!=null)placeholder.setVisibility(View.GONE);
        }
        if(current.key.equals("item_grid")||current.key.equals("item_list")){
            TextView title=sourceRoot.findViewById(R.id.text_title);if(title!=null)title.setText("Jogo demonstrativo");
            TextView author=sourceRoot.findViewById(R.id.text_author);if(author!=null)author.setText("Item visual original do SKYrus");
            ImageView image=sourceRoot.findViewById(R.id.icon);if(image!=null)image.setImageResource(R.drawable.skymu_icon_new);
        }
    }
    private static class Holder extends RecyclerView.ViewHolder{Holder(View v){super(v);}}
    private void setupPreviewClicks(){
        if(current==null||sourceRoot==null)return;
        Map<Integer,String> links=new HashMap<>();
        if(current.key.equals("home")){
            links.put(R.id.grid_button,"item_grid");links.put(R.id.search_button,"home_search");
            links.put(R.id.controller_hints,"home_menu");
            links.put(R.id.search_panel,"home_search");
        }
        if(current.key.equals("home_menu")){
            links.put(R.id.home_menu_settings,"settings_system");
            links.put(R.id.home_menu_overlays,"settings_overlay");
            links.put(R.id.home_menu_content,"settings_content");
            links.put(R.id.home_menu_frame_generation,"settings_frame");
            links.put(R.id.home_menu_controls,"settings_input");
            links.put(R.id.home_menu_driver,"driver");
        }
        for(Map.Entry<Integer,String> e:links.entrySet()){View v=sourceRoot.findViewById(e.getKey());if(v!=null)v.setOnClickListener(w->showScene(e.getValue()));}
        if(current.key.equals("home_search")){View close=sourceRoot.findViewById(R.id.search_dialog_close);if(close!=null)close.setOnClickListener(v->showScene("home"));}
    }
    private void captureOriginal(View v){
        original.put(v,new float[]{v.getTranslationX(),v.getTranslationY(),v.getScaleX(),v.getScaleY(),v.getAlpha()});
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)captureOriginal(g.getChildAt(i));}
    }
    private void captureOriginalMissing(View v){
        if(!original.containsKey(v)) original.put(v,new float[]{v.getTranslationX(),v.getTranslationY(),v.getScaleX(),v.getScaleY(),v.getAlpha()});
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)captureOriginalMissing(g.getChildAt(i));}
    }
    private String viewKey(View v){
        if(current==null||sourceRoot==null)return null;
        if(v==sourceRoot)return "root";
        int id=v.getId();
        if(id!=View.NO_ID){
            try{if(sourceRoot.findViewById(id)==v)return getResources().getResourceEntryName(id);}
            catch(Exception ignored){}
        }
        StringBuilder path=new StringBuilder();View cursor=v;
        while(cursor!=sourceRoot && cursor.getParent() instanceof ViewGroup){
            ViewGroup g=(ViewGroup)cursor.getParent();path.insert(0,"/"+g.indexOfChild(cursor));cursor=g;
        }
        return "path"+path;
    }
    private void applySavedTransforms(View v){
        JSONObject byScene=changes.optJSONObject(current.key);
        if(byScene!=null){JSONObject change=byScene.optJSONObject(viewKey(v));if(change!=null){
            float[] base=original.get(v);
            if(base!=null){float k=(float)change.optDouble("size",1);
                v.setTranslationX(base[0]+d((float)change.optDouble("dx",0)));
                v.setTranslationY(base[1]+d((float)change.optDouble("dy",0)));
                v.setScaleX(base[2]*k);v.setScaleY(base[3]*k);
                v.setAlpha((float)change.optDouble("alpha",base[4]));}
        }}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)applySavedTransforms(g.getChildAt(i));}
    }
    private void rememberSelected(){
        if(selected==null||current==null)return;
        String key=viewKey(selected);float[] base=original.get(selected);if(base==null||key==null)return;
        JSONObject record=new JSONObject();
        try{
            record.put("dx",(selected.getTranslationX()-base[0])/getResources().getDisplayMetrics().density);
            record.put("dy",(selected.getTranslationY()-base[1])/getResources().getDisplayMetrics().density);
            record.put("size",selected.getScaleX()/Math.max(.001f,base[2]));
            record.put("alpha",selected.getAlpha());
            JSONObject scene=changes.optJSONObject(current.key);if(scene==null){scene=new JSONObject();changes.put(current.key,scene);}
            scene.put(key,record);
            selectedName.setText("Selecionado: "+key+" (view real)");
            offsetLabel.setText(String.format(Locale.getDefault(),"Deslocamento: %.0f × %.0f dp",record.optDouble("dx"),record.optDouble("dy")));
        }catch(Exception ignored){}
    }
    private void select(View v){selected=v;updateSliders();overlay.invalidate();}
    private void updateSliders(){
        changingWidgets=true;
        float[] base=original.get(selected);
        int size=100,alpha=100;
        if(selected!=null&&base!=null){size=Math.round(100*selected.getScaleX()/Math.max(.001f,base[2]));alpha=Math.round(selected.getAlpha()*100);}
        sizeSlider.setProgress(Math.min(sizeSlider.getMax(),Math.max(0,size)));alphaSlider.setProgress(alpha);
        sizeSlider.setEnabled(selected!=null);alphaSlider.setEnabled(selected!=null);
        scaleLabel.setText("Tamanho: "+size+"%");opacityLabel.setText("Transparência: "+alpha+"%");
        selectedName.setText(selected==null?"Toque em um elemento real para selecioná-lo":"Selecionado: "+viewKey(selected));
        offsetLabel.setText("Arraste o item com o dedo para posicionar");
        changingWidgets=false;
    }
    private void resetSelected(){if(selected==null)return;
        JSONObject screen=changes.optJSONObject(current.key);if(screen!=null)screen.remove(viewKey(selected));
        float[] p=original.get(selected);if(p!=null){selected.setTranslationX(p[0]);selected.setTranslationY(p[1]);selected.setScaleX(p[2]);selected.setScaleY(p[3]);selected.setAlpha(p[4]);}
        updateSliders();overlay.invalidate();save();
    }
    private void resetScreen(){new AlertDialog.Builder(this).setTitle("Restaurar tela?").setMessage("Apagar as alterações desta tela?")
        .setNegativeButton("Cancelar",null).setPositiveButton("Restaurar",(d,w)->{changes.remove(current.key);save();showScene(current.key);}).show();}
    private void save(){getPreferences(MODE_PRIVATE).edit().putString(DOC,changes.toString()).apply();if(status!=null)status.setText("Alterações locais salvas · o SKYrus original não foi modificado");}
    private void chooseFile(boolean export){Intent i=new Intent(export?Intent.ACTION_CREATE_DOCUMENT:Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");if(export)i.putExtra(Intent.EXTRA_TITLE,"skyrus-real-ui-transforms.json");
        startActivityForResult(i,export?EXPORT:IMPORT);}
    @Override protected void onActivityResult(int request,int result,@Nullable Intent data){super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{
            if(request==EXPORT){JSONObject doc=new JSONObject();doc.put("format",FORMAT);doc.put("version",1);
                doc.put("origin","SKYrus-SKYmu-Principal (1) (8).zip");doc.put("edits",changes);
                try(OutputStream os=getContentResolver().openOutputStream(uri)){if(os==null)throw new IllegalStateException("Destino indisponível");os.write(doc.toString(2).getBytes(StandardCharsets.UTF_8));}
                status.setText("JSON exportado. Requer integração no SKYrus para aplicar no emulador.");
            }else if(request==IMPORT){
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream is=getContentResolver().openInputStream(uri)){
                    if(is==null)throw new IllegalStateException("Arquivo indisponível");byte[] chunk=new byte[8192];int n;
                    while((n=is.read(chunk))!=-1){if(bytes.size()+n>1024*1024)throw new IllegalArgumentException("Arquivo grande demais");bytes.write(chunk,0,n);}}
                JSONObject doc=new JSONObject(bytes.toString("UTF-8"));if(!FORMAT.equals(doc.optString("format"))||doc.optInt("version")!=1)throw new IllegalArgumentException("Formato incompatível");
                JSONObject edits=doc.getJSONObject("edits");if(edits.length()>50)throw new IllegalArgumentException("Telas demais");
                changes=edits;save();showScene(current.key);status.setText("Transformações importadas.");
            }
        }catch(Exception e){new AlertDialog.Builder(this).setTitle("Erro de arquivo").setMessage(e.toString()).setPositiveButton("OK",null).show();}
    }
    @Override protected void onPause(){save();super.onPause();}

    private class TouchOverlay extends View{
        private Paint border=new Paint(3);private float touchX,touchY,startX,startY;private boolean dragging;
        TouchOverlay(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(selected==null||selected.getWidth()<=0)return;
            Rect rect=new Rect();if(!selected.getGlobalVisibleRect(rect))return;
            int[] origin=new int[2];getLocationOnScreen(origin);
            border.setColor(0xff26b8fb);border.setStrokeWidth(d(2));border.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(rect.left-origin[0],rect.top-origin[1],rect.right-origin[0],rect.bottom-origin[1],d(6),d(6),border);
            border.setStyle(Paint.Style.FILL);canvas.drawCircle(rect.right-origin[0],rect.bottom-origin[1],d(6),border);
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(!editing)return false;
            switch(e.getActionMasked()){
                case MotionEvent.ACTION_DOWN:{
                    View hit=deepest(sourceRoot, (int)e.getRawX(),(int)e.getRawY());
                    if(hit==sourceRoot)hit=null;
                    select(hit);touchX=e.getRawX();touchY=e.getRawY();
                    if(hit!=null){startX=hit.getTranslationX();startY=hit.getTranslationY();}dragging=false;
                    return true;
                }
                case MotionEvent.ACTION_MOVE:{
                    if(selected!=null){float dx=e.getRawX()-touchX,dy=e.getRawY()-touchY;
                        if(Math.abs(dx)+Math.abs(dy)>d(2)){dragging=true;selected.setTranslationX(startX+dx);selected.setTranslationY(startY+dy);
                        rememberSelected();invalidate();}}
                    return true;
                }
                case MotionEvent.ACTION_UP:{if(dragging)save();return true;}
                case MotionEvent.ACTION_CANCEL:return true;
            }return true;
        }
        private View deepest(View target,int x,int y){
            if(target==null||target.getVisibility()!=View.VISIBLE||target.getAlpha()==0)return null;
            Rect r=new Rect();if(!target.getGlobalVisibleRect(r)||!r.contains(x,y))return null;
            if(target instanceof ViewGroup){ViewGroup g=(ViewGroup)target;
                for(int i=g.getChildCount()-1;i>=0;i--){View v=deepest(g.getChildAt(i),x,y);if(v!=null)return v;}}
            return target;
        }
    }

    /** Actual source XML hierarchy, with emulator-specific preference classes mapped to AndroidX.
     *  This is an independent preview: settings are NOT synchronized with SKYrus. */
    public static class PreviewPrefsFragment extends PreferenceFragmentCompat{
        static PreviewPrefsFragment newInstance(String section){PreviewPrefsFragment f=new PreviewPrefsFragment();
            Bundle args=new Bundle();args.putString("section",section);f.setArguments(args);return f;}
        @Override public void onCreatePreferences(Bundle b,String rootKey){
            String section=getArguments()==null?"system":getArguments().getString("section","system");
            switch(section){
                case "content": addPreferencesFromResource(R.xml.preview_content_preferences);break;
                case "input": addPreferencesFromResource(R.xml.preview_input_preferences);break;
                case "overlay": addPreferencesFromResource(R.xml.preview_overlay_preferences);break;
                case "frame": addPreferencesFromResource(R.xml.preview_frame_generation_preferences);break;
                default:
                    addPreferencesFromResource(R.xml.preview_app_preferences);
                    addPreferencesFromResource(R.xml.preview_emulation_preferences);
                    addPreferencesFromResource(R.xml.preview_credits_preferences);
                    String selected=section.equals("about")?"licenses":section;
                    for(String category:new String[]{"system","graphics","audio","appearance","debug","licenses"}){
                        PreferenceCategory c=findPreference("category_"+category);if(c!=null)c.setVisible(category.equals(selected));
                    }
            }
        }
    }
}
