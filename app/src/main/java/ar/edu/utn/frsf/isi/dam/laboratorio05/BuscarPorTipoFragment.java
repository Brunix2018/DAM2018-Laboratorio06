package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import ar.edu.utn.frsf.isi.dam.laboratorio05.R;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class BuscarPorTipoFragment extends Fragment {




    private ReclamoDao reclamoDao;
    private Spinner tipoReclamo;
    private Button buscarTipo;
    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;

    /************** OnNuevoLugarListener *********************/
    private OnTipoBusquedaListener listener;

    public interface OnTipoBusquedaListener {
        public void mostrarBusquedaTipo(String tipo);
    }

    public OnTipoBusquedaListener getListener() {
        return listener;
    }

    public void setListener(OnTipoBusquedaListener listener) {
        this.listener = listener;
    }


    /************** OnNuevoLugarListener *********************/



    public BuscarPorTipoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_busqueda, container, false);


        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo2);

        buscarTipo= (Button) v.findViewById(R.id.btnBuscarTipo);


        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);




        tipoReclamo.setEnabled(true);
        buscarTipo.setEnabled(true);
        buscarTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.mostrarBusquedaTipo(tipoReclamo.getSelectedItem().toString());

            }
        });

        return v;
    }







}
