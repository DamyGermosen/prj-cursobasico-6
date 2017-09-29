package com.example.dgermosen.myproject_imc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.icu.text.DecimalFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String[] Estatura = {"cm", "mt", "pie"}; //Unidades de medidas para la estatura
    private String[] Peso = {"kg", "lb"}; //Unidades de medidas para el peso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnCalcular).setOnClickListener(this);

        String[] Edades = new String[49];
        int EdadMenor=18;
        //El IMC se calcula generalmente para personas entre 18 y 65 años (siendo las personas de más de 65 años
        //parte de éste último rango de porcentajes de IMC, por ello el arreglo va de 18 a 65+ años
        for (int i = 0; i < Edades.length; i++)
        {
            Edades[i] = Integer.toString(EdadMenor);
            EdadMenor++;
        }
        Edades[48] = "65+" ;

        Spinner spnEdad = (Spinner) findViewById(R.id.spnEdad);

        ArrayAdapter<String> EdadAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Edades);
        EdadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnEdad.setAdapter(EdadAdapter);

        Spinner spnEstatura = (Spinner) findViewById(R.id.spnEstatura);

        ArrayAdapter<String> EstaturaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Estatura);
        EstaturaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnEstatura.setAdapter(EstaturaAdapter);

        Spinner spnPeso = (Spinner) findViewById(R.id.spnPeso);

        ArrayAdapter<String> PesoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Peso);
        PesoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPeso.setAdapter(PesoAdapter);

        RadioGroup rgrSexo = (RadioGroup) findViewById(R.id.rgrSexo);
        rgrSexo.requestFocus();

    }
    //Método que se ejecuta al presionar el botón BACK en el dispositivo móvil
    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        finish();
        return;
    }
    public void onClick(View view) {
        InputMethodManager inputManager =
                (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        Calcular_IMC(); //Ejecución método calculará IMC
    }
    //Método para limpiar la pantalla en caso de algún error o simplemente porque el usuario desee
    //calcular otro IMC
    public void Limpiar(View view) {
        RadioGroup rgrSexo = (RadioGroup) findViewById(R.id.rgrSexo);
        Spinner spnEdad = (Spinner) findViewById(R.id.spnEdad);
        Spinner spnEstatura = (Spinner) findViewById(R.id.spnEstatura);
        Spinner spnPeso = (Spinner) findViewById(R.id.spnPeso);
        EditText txtPeso = (EditText) findViewById(R.id.txtPeso);
        EditText txtEstatura = (EditText) findViewById(R.id.txtEstatura);

        rgrSexo.clearCheck();
        spnEdad.setSelection(0);
        spnEstatura.setSelection(0);
        spnPeso.setSelection(0);
        txtEstatura.setText(null);
        txtPeso.setText(null);

        TextView lblTipoPeso = (TextView) findViewById(R.id.lblTipoPeso);
        lblTipoPeso.setText(null);
        lblTipoPeso.setVisibility(View.INVISIBLE);
        ImageView imgTipoPeso = (ImageView) findViewById(R.id.imgTipoPeso);
        imgTipoPeso.setVisibility(View.INVISIBLE);
        TextView lblResultado = (TextView) findViewById(R.id.lblResultado);
        lblResultado.setVisibility(View.INVISIBLE);
    }
    //Método Calcular_IMC, determina el IMC de acuerdo a la fórmula general
    //Peso en kilogramos / el cuadrado de la estatura en mts
    public void Calcular_IMC() {

        RadioGroup rgrSexo = (RadioGroup) findViewById(R.id.rgrSexo);
        Spinner spnEdad = (Spinner) findViewById(R.id.spnEdad);
        Spinner spnEstatura = (Spinner) findViewById(R.id.spnEstatura);
        Spinner spnPeso = (Spinner) findViewById(R.id.spnPeso);
        EditText txtPeso = (EditText) findViewById(R.id.txtPeso);
        EditText txtEstatura = (EditText) findViewById(R.id.txtEstatura);

        double Peso, Estatura, EstaturaPulg, Resultado = 0;
        int Edad, EstaturaPie;
        char Sexo=' ';

        //Si se elije 65+ pues es lo mismo que 65 porque forma parte del mismo rango de IMC
        if (spnEdad.getSelectedItem()=="65+")
            Edad = 65;
        else
            Edad = Integer.parseInt(spnEdad.getSelectedItem().toString());

        //Validar se completen los datos correspondientes a la estatura y el peso.
        if(txtEstatura.getText().toString().matches("") || txtPeso.getText().toString().matches("")) {
            Toast.makeText(this, "Debes completar los datos para estatura y peso!", Toast.LENGTH_SHORT).show();
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200);
            return;
        }

        //Si se elige unidad de medida para estatura metro (mt), no requiere conversión para calcular IMC
        if (spnEstatura.getSelectedItem()=="mt")
            Estatura = Float.parseFloat(txtEstatura.getText().toString());
        //En caso de que la unidad de medida sea centímetro (cm) debe convertirse a mt, sabiendo que un
        //mt tiene 100 cm, se divide el valor digitado en Estatura sobre 100
        else if (spnEstatura.getSelectedItem()=="cm")
            Estatura = Float.parseFloat(txtEstatura.getText().toString())/100;
        //En el caso en que no se elijió mt ni cm, el usuario selección pies, por tanto, hay que convertir
        //la parte entera del valor de la estatura a mt, sabiendo que 1 pie equivale a 0.3048 mt
        //y la parte decimal (representa las pulgadas) se convierte a mt, sabiendo que 1 pulgada
        //equivale 0.0254 mt
        else
        {
            Estatura = Float.parseFloat(txtEstatura.getText().toString());
            EstaturaPie = (int)(Estatura);
            EstaturaPulg = (Estatura - (double)(EstaturaPie))*100 ;
            Estatura = EstaturaPie*0.3048 + EstaturaPulg*0.0254;
        }

        //Si la Unidad de Medida es kg, solo obtener el dato digitado en el campo de peso
        if (spnPeso.getSelectedItem()=="kg")
            Peso = Float.parseFloat(txtPeso.getText().toString());
        //Si la unidad de medida elegida fue lbs, entonces se debe convertir de lbs a kg
        //sabiendo que 1 kg equivale a 2.2048 lbs
        else
            Peso = Float.parseFloat(txtPeso.getText().toString())/2.2048;

        RadioButton rbSeleccionado = (RadioButton) findViewById(rgrSexo.getCheckedRadioButtonId());

        //Verificar la selección del sexo
        if (rbSeleccionado != null)
        {
            if(rbSeleccionado.getText().toString().equalsIgnoreCase("Hombre"))
                Sexo='H';
            else
                Sexo='M';

            //Calcular IMC, en base a que IMC = Peso en kg / Estatura en Mt al Cuadrado
            Resultado = Peso / (Estatura*Estatura);
            //Mostrar en pantalla el rango del IMC en base a la edad, sexo y el valor del IMC obtenido anteriormente
            Mostrar_Tipo_IMC(Edad,Sexo,Resultado);
        }

        //Validar el sexo haya sido seleccionado
        else
            {
                Toast.makeText(this, "Debes seleccionar el sexo!", Toast.LENGTH_SHORT).show();
                return;
            }

    }

    //Método Obtener_Tipo_Peso_Hombre
    //Para sexo masculino, verificar que de acuerdo a los rangos de edad establecidos (18 a 24, 25 a 34, 35 a 44, 45 a 54, 54 a 65, 65 o más)
    //Y el resultado del IMC enviado, qué tipo de masa corporal corresponde, si peso insuficiente,
    //normal, sobrepeso, obeso u obesidad extrema
    public char Obtener_Tipo_Peso_Hombre(int Edad, double IMC)
    {
        if(Edad>=65)
        {
            if(IMC>45.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>35.0)
                return 'O'; //Obeso
            else if(IMC>30.0)
                return 'S'; //Sobrepeso
            else if(IMC>24.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=54)
        {
            if(IMC>44.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>34.0)
                return 'O'; //Obeso
            else if(IMC>29.0)
                return 'S'; //Sobrepeso
            else if(IMC>23.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=45)
        {
            if(IMC>43.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>33.0)
                return 'O'; //Obeso
            else if(IMC>28.0)
                return 'S'; //Sobrepeso
            else if(IMC>22.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=35)
        {
            if(IMC>42.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>32.0)
                return 'O'; //Obeso
            else if(IMC>27.0)
                return 'S'; //Sobrepeso
            else if(IMC>21.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=25)
        {
            if(IMC>41.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>31.0)
                return 'O'; //Obeso
            else if(IMC>26.0)
                return 'S'; //Sobrepeso
            else if(IMC>20.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        //18 - 24 años
        else
        {
            if(IMC>40.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>30.0)
                return 'O'; //Obeso
            else if(IMC>25.0)
                return 'S'; //Sobrepeso
            else if(IMC>19.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }

    }

    //Método Obtener_Tipo_Peso_Mujer
    //Para sexo femenino, verificar que de acuerdo a los rangos de edad establecidos (18 a 24, 25 a 34, 35 a 44, 45 a 54, 54 a 65, 65 o más)
    //Y el resultado del IMC enviado, qué tipo de masa corporal corresponde, si peso insuficiente,
    //normal, sobrepeso, obeso u obesidad extrema

    public char Obtener_Tipo_Peso_Mujer(int Edad, double IMC)
    {
        if(Edad>=65)
        {
            if(IMC>44.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>34.0)
                return 'O'; //Obeso
            else if(IMC>29.0)
                return 'S'; //Sobrepeso
            else if(IMC>23.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=54)
        {
            if(IMC>43.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>33.0)
                return 'O'; //Obeso
            else if(IMC>28.0)
                return 'S'; //Sobrepeso
            else if(IMC>22.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=45)
        {
            if(IMC>42.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>32.0)
                return 'O'; //Obeso
            else if(IMC>27.0)
                return 'S'; //Sobrepeso
            else if(IMC>21.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=35)
        {
            if(IMC>41.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>31.0)
                return 'O'; //Obeso
            else if(IMC>26.0)
                return 'S'; //Sobrepeso
            else if(IMC>20.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        else if(Edad>=25)
        {
            if(IMC>40.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>30.0)
                return 'O'; //Obeso
            else if(IMC>25.0)
                return 'S'; //Sobrepeso
            else if(IMC>19.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
        //18 - 24 años
        else
        {
            if(IMC>39.0)
                return 'E'; //Obesidad Extrema
            else if(IMC>29.0)
                return 'O'; //Obeso
            else if(IMC>24.0)
                return 'S'; //Sobrepeso
            else if(IMC>18.0)
                return 'N'; //Peso normal
            else
                return 'I'; //Peso insuficiente
        }
    }
    //Mostrar_Tipo_IMC
    //Mostrar en la aplicación el rango obtenido de IMC
    public void Mostrar_Tipo_IMC(int Edad, char Sexo, double IMC)
    {
        MediaPlayer mp;
        TextView lblTipoPeso = (TextView) findViewById(R.id.lblTipoPeso);
        lblTipoPeso.setVisibility(View.VISIBLE);
        ImageView imgTipoPeso = (ImageView) findViewById(R.id.imgTipoPeso);
        imgTipoPeso.setVisibility(View.VISIBLE);
        TextView lblResultado = (TextView) findViewById(R.id.lblResultado);
        lblResultado.setVisibility(View.VISIBLE);
        lblResultado.setText("Resultado: " + String.format("%.2f",IMC));

        char TipoPeso = ' ';
        //Si el cálculo IMC es para hombre
        if (Sexo == 'H')
            TipoPeso = Obtener_Tipo_Peso_Hombre(Edad, IMC);
        else
            TipoPeso = Obtener_Tipo_Peso_Mujer(Edad, IMC);

        //IMC Obesidad Extrema
        if (TipoPeso == 'E') {
            lblResultado.setTextColor(Color.parseColor("#D94551"));
            lblTipoPeso.setText("Clasificación: OBESIDAD EXTREMA.");
            lblTipoPeso.setBackgroundColor(Color.parseColor("#D94551"));
            imgTipoPeso.setImageResource(R.mipmap.extrema);
            mp = MediaPlayer.create(MainActivity.this, R.raw.obesidadextrema);
            mp.start();
        }
        //IMC Obeso
        else if (TipoPeso == 'O') {
            lblResultado.setTextColor(Color.parseColor("#FE7E37"));
            lblTipoPeso.setText("Clasificación: OBESO.");
            lblTipoPeso.setBackgroundColor(Color.parseColor("#FE7E37"));
            imgTipoPeso.setImageResource(R.mipmap.obeso);
            mp = MediaPlayer.create(MainActivity.this, R.raw.obeso);
            mp.start();
        }
        //IMC Sobrepeso
        else if (TipoPeso == 'S') {
            lblResultado.setTextColor(Color.parseColor("#FFB98D"));
            lblTipoPeso.setText("Clasificación: SOBREPESO.");
            lblTipoPeso.setBackgroundColor(Color.parseColor("#FFB98D"));
            imgTipoPeso.setImageResource(R.mipmap.sobrepeso);
            mp = MediaPlayer.create(MainActivity.this, R.raw.sobrepeso);
            mp.start();
        }
        //IMC peso normal
        else if (TipoPeso == 'N') {
            lblResultado.setTextColor(Color.parseColor("#6AD0D5"));
            lblTipoPeso.setText("Clasificación: PESO NORMAL.");
            lblTipoPeso.setBackgroundColor(Color.parseColor("#6AD0D5"));
            imgTipoPeso.setImageResource(R.mipmap.normal);
            mp = MediaPlayer.create(MainActivity.this, R.raw.normal);
            mp.start();
        }
        //IMC peso insuficiente
        else {
            lblResultado.setTextColor(Color.parseColor("#006B99"));
            lblTipoPeso.setText("Clasificación: PESO INSUFICIENTE.");
            lblTipoPeso.setBackgroundColor(Color.parseColor("#006B99"));
            imgTipoPeso.setImageResource(R.mipmap.insuficiente);
            mp = MediaPlayer.create(MainActivity.this, R.raw.insuficiente);
            mp.start();
        }

        Toast.makeText(this, lblTipoPeso.getText(), Toast.LENGTH_LONG).show();
    }
}
