package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class NuevoReclamoFragment extends Fragment {



    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private ImageView foto;
    private Button btnSacarFoto;
    private Button btnGrabarAudio;
    private Button btnReproducirAudio;
    private String pathFoto="";


    /******AUDIO*****/
    private String pathAudio="";
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private Boolean grabando = false;
    private Boolean reproduciendo = false;
    private static final int REQUEST_MICROPHONE = 2007;
    private File mAudioFile;
    /******AUDIO*****/


    /************** OnNuevoLugarListener *********************/
    private OnNuevoLugarListener listener;

    public interface OnNuevoLugarListener {
        public void obtenerCoordenadas();
        public void sacarFoto();
        public File createAudioFile();
    }

    public OnNuevoLugarListener getListener() {
        return listener;
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }


    /************** OnNuevoLugarListener *********************/





    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail= (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord= (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord= (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar= (Button) v.findViewById(R.id.btnGuardar);
        btnSacarFoto = (Button) v.findViewById(R.id.btnFoto);
        foto = (ImageView) v.findViewById(R.id.imageView);

        /******AUDIO*****/

        btnGrabarAudio= (Button) v.findViewById(R.id.btnGrabarAudio);
        btnReproducirAudio = (Button) v.findViewById(R.id.btnRproducirAudio);
        btnGrabarAudio.setOnClickListener(listenerPlayer);
        btnReproducirAudio.setOnClickListener(listenerPlayer);

        mAudioFile= listener.createAudioFile();
        pathAudio= mAudioFile.getAbsolutePath();


        /******AUDIO*****/

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo =0;
        if(getArguments()!=null)  {
            idReclamo = getArguments().getInt("idReclamo",0);

        }

        cargarFoto();
        cargarReclamo(idReclamo);


        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada );
        mail.setEnabled(edicionActivada );
        tipoReclamo.setEnabled(edicionActivada);
        btnReproducirAudio.setEnabled(edicionActivada);
        btnGrabarAudio.setEnabled(edicionActivada);
        btnSacarFoto.setEnabled(true);
        btnGuardar.setEnabled(edicionActivada);

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();

            }
        });

        btnSacarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    listener.sacarFoto();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2000);


                }
            }
        });


        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });
        return v;
    }

    private void cargarReclamo(final int id){
        if( id >0){
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud()+";"+reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());

                            Reclamo.TipoReclamo[] tipos= Reclamo.TipoReclamo.values();
                            for(int i=0;i<tipos.length;i++) {
                                if(tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        }else{
            String coordenadas = "0;0";
            if(getArguments()!=null) coordenadas = getArguments().getString("latLng","0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo(){
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if(tvCoord.getText().toString().length()>0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
            reclamoActual.setFoto(pathFoto);
            reclamoActual.setAudio(pathAudio);
        }
        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {

                if(reclamoActual.getId()>0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        getActivity().getFragmentManager().popBackStack();
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }


    private void cargarFoto(){
        if (!pathFoto.equals("")) {
            System.out.println("####cargarFoto####");
            File file = new File(pathFoto);
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageBitmap != null) {
                foto.setImageBitmap(imageBitmap);
            }
        }else{
            System.out.println("######PATH VACIO####");
        }
    }

    public void setPathFoto(String pathFoto) {
        this.pathFoto = pathFoto;
        cargarFoto();

    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("reclamoDesc", reclamoDesc.getText().toString());
        outState.putString("mail", mail.getText().toString());
        outState.putString("tvCoord", tvCoord.getText().toString());
        outState.putString("pathFoto", pathFoto);
    }


    public void onRestoreInstanceState( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reclamoDesc.setText(savedInstanceState.getString("reclamoDesc"));
        mail.setText(savedInstanceState.getString("mail"));
        tvCoord.setText(savedInstanceState.getString("tvCoord"));
        pathFoto=(savedInstanceState.getString("pathFoto"));
    }

    /******AUDIO*****/



    private void grabar() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.RECORD_AUDIO);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(pathAudio);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("falla", "prepare() failed");
            }
            mRecorder.start();

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_MICROPHONE);
        }
    }
    private void terminarGrabar() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void reproducir() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(pathAudio);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("falla", "prepare() failed");
        }
    }

    private void terminarReproducir() {
        mPlayer.release();
        mPlayer = null;
    }

    private View.OnClickListener listenerPlayer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.btnRproducirAudio:              if(reproduciendo){
                        ((Button) view).setText("Reproducir");
                        reproduciendo=false;
                        terminarReproducir();
                    }else{
                        ((Button) view).setText("pausar.....");
                        reproduciendo=true;
                        reproducir();
                    }
                    break;
                case R.id.btnGrabarAudio:
                    if(grabando){
                        ((Button) view).setText("Grabar");
                        grabando=false;
                        terminarGrabar();
                    }else{
                        ((Button) view).setText("grabando.....");
                        grabando=true;
                        grabar();

                    }
                    break;
            }
        }
    };


}
