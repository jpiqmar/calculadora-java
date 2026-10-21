package es.upv.etsit.calculadora;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/*
Gonzalo López de Villalta Hernández
Jaime Piqueras Marsal
Marc Zaragoza García

FUNCIONES NUEVAS AÑADIDAS:
Operacion de raiz cuadrada y elevado al cuadrado que se ejecutan automaticamente en el numero que haya en la calculadora cuando
su tecla en cuestión es presionada.
Operación de porcentaje que muestra el resto de la división, funciona como las operaciones anteriores.

ERRORES CORREGIDOS Y MEJORAS:
Control de la división entre 0 -> se muestra "Error".
Límite de dígitos que se pueden escribir en pantalla, para que el número no se salga del TextView.
Separador de miles en los resultados (ej. 1,234.56), para leer mejor los números grandes.
 */


public class MainActivity extends AppCompatActivity {

    //Variables necesarias:
    enum OPERACION { SUMAR, RESTAR, MULTIPLICAR, DIVIDIR, IGUAL, RAIZ, CUADRADO, PORCENTAJE}
    OPERACION OP_ANT;
    enum ESTADOS {INIC, NUMERO, OPERACION}
    ESTADOS estado;
    double acumulador;
    String operando2_str;
    double operando2;
    TextView display;
    TextView displayHistorial;
    String historial1 = "";
    String historial2 = "";
    DecimalFormat df;
    private static final int MAX_DIGITOS = 12;


    // Configuración inicial de la Actividad, interfaz gráfica y formato de números
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        display = (TextView)findViewById(R.id.display);
        displayHistorial = (TextView)findViewById(R.id.tv_historial);

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        df = new DecimalFormat("#,##0.#############", decimalFormatSymbols);

        aINIC();
    }

    // Inicializa o reinicia las variables de la calculadora al estado por defecto
    private void aINIC() {
        operando2_str   = "";
        display.setText("0");
        operando2 = 0.;
        acumulador = 0.;
        OP_ANT = OPERACION.IGUAL;
        estado = ESTADOS.INIC;

        historial1 = "";
        historial2 = "";
        if(displayHistorial != null) displayHistorial.setText("");
    }

    // Maneja los errores (como la división por cero) reseteando variables y mostrando "Error"
    private void mostrarError() {
        operando2_str = "";
        operando2 = 0.;
        acumulador = 0.;
        OP_ANT = OPERACION.IGUAL;
        estado = ESTADOS.INIC;
        display.setText("Error");
    }

    // Metodo principal que gestiona la logica de la calculadora
    public void alPulsarTecla(View tecla){
        int id = tecla.getId();

        // Gestión del botón del punto decimal
        if ( id == R.id.b_punto ) {
            if (operando2_str.contains(".")) return;
            if (operando2_str.length() >= MAX_DIGITOS) return;
            operando2_str += ((TextView) tecla).getText().toString();
            operando2 = miStringToDouble(operando2_str);
            display.setText(operando2_str);
            estado = ESTADOS.NUMERO;
        }
        // Gestión de los botones numéricos (0-9)
        else if ( id==R.id.b_0 || id==R.id.b_1 || id==R.id.b_2 || id==R.id.b_3 ||
                id==R.id.b_4 || id==R.id.b_5 || id==R.id.b_6 || id==R.id.b_7 ||
                id==R.id.b_8 || id==R.id.b_9 ) {
            if (operando2_str.length() >= MAX_DIGITOS) return;
            operando2_str += ((TextView) tecla).getText().toString();
            operando2 = miStringToDouble(operando2_str);
            display.setText(operando2_str);
            estado = ESTADOS.NUMERO;
        }
        // Gestión de las operaciones aritméticas en cadena (+, -, *, /, %, =)
        else if ( id == R.id.b_mas || id == R.id.b_menos || id == R.id.b_por || id == R.id.b_div || id == R.id.b_igual || id == R.id.b_porcentaje ) {

            if (estado == ESTADOS.OPERACION) {
                if (id == R.id.b_mas) OP_ANT = OPERACION.SUMAR;
                else if (id == R.id.b_menos) OP_ANT = OPERACION.RESTAR;
                else if (id == R.id.b_por) OP_ANT = OPERACION.MULTIPLICAR;
                else if (id == R.id.b_div) OP_ANT = OPERACION.DIVIDIR;
                else if (id == R.id.b_porcentaje) OP_ANT = OPERACION.PORCENTAJE;
                return;
            }

            // Prevención de errores matemáticos
            if (OP_ANT == OPERACION.DIVIDIR && operando2 == 0) {
                mostrarError();
                return;
            }

            String operacionStr = obtenerSimboloOp(OP_ANT);
            String calculoActual = "";

            if (OP_ANT != OPERACION.IGUAL) {
                calculoActual = miDoubleToString(acumulador) + " " + operacionStr + " " + miDoubleToString(operando2);
            }

            acumulador = operar(acumulador, OP_ANT, operando2);
            display.setText(miDoubleToString(acumulador));

            if (id == R.id.b_igual && !calculoActual.isEmpty()) {
                historial2 = historial1;
                historial1 = calculoActual + " = " + miDoubleToString(acumulador);
                if (displayHistorial != null) {
                    displayHistorial.setText(historial2 + "\n" + historial1);
                }
            }

            if (id == R.id.b_mas) OP_ANT = OPERACION.SUMAR;
            else if (id == R.id.b_menos) OP_ANT = OPERACION.RESTAR;
            else if (id == R.id.b_por) OP_ANT = OPERACION.MULTIPLICAR;
            else if (id == R.id.b_div) OP_ANT = OPERACION.DIVIDIR;
            else if (id == R.id.b_porcentaje) OP_ANT = OPERACION.PORCENTAJE;
            else if (id == R.id.b_igual) OP_ANT = OPERACION.IGUAL;

            operando2_str = "";
            operando2 = 0.;
            estado = ESTADOS.OPERACION;
        }
        // Bloque exclusivo para Raíz y Cuadrado (Operaciones instantáneas)
        else if ( id == R.id.b_raiz || id == R.id.b_cuadrado ) {
            // Detectamos qué número está activo actualmente para aplicarle la operación
            double valorAOperar = (estado == ESTADOS.NUMERO) ? operando2 : acumulador;
            double resultado = 0;
            String calculoActual = "";

            if (id == R.id.b_raiz) {
                if (valorAOperar < 0) {
                    mostrarError();
                    return;
                }
                resultado = Math.sqrt(valorAOperar);
                calculoActual = "√(" + miDoubleToString(valorAOperar) + ")";
            } else {
                resultado = Math.pow(valorAOperar, 2);
                calculoActual = "(" + miDoubleToString(valorAOperar) + ")²";
            }

            // Guardamos el resultado en la variable que corresponda según el estado actual
            if (estado == ESTADOS.NUMERO) {
                operando2 = resultado;
                operando2_str = miDoubleToString(operando2);
                display.setText(operando2_str);
            } else {
                acumulador = resultado;
                display.setText(miDoubleToString(acumulador));
            }

            // Actualizamos el historial inmediatamente
            historial2 = historial1;
            historial1 = calculoActual + " = " + miDoubleToString(resultado);
            if (displayHistorial != null) {
                displayHistorial.setText(historial2 + "\n" + historial1);
            }
        }
        // Alterna el signo del número en pantalla (positivo a negativo y viceversa)
        else if ( id == R.id.b_signo ) {
            if (estado == ESTADOS.NUMERO) {
                operando2 = -operando2;
                operando2_str = miDoubleToString(operando2);
                display.setText(operando2_str);
            } else {
                acumulador = -acumulador;
                display.setText(miDoubleToString(acumulador));
            }
        }
        // Botón de limpieza total (Clear) de la entrada actual
        else if ( id == R.id.clear ) {
            if (estado == ESTADOS.NUMERO) {
                operando2 = 0;
                operando2_str = miDoubleToString(operando2);
                display.setText(operando2_str);
            } else {
                acumulador = 0;
                display.setText(miDoubleToString(acumulador));
            }
        }
    }

    // Función de operar según la operacion asignada (Raiz y cuadrado no porque son instantaneas)
    private double operar (double operando1, OPERACION operacion, double operando2) {
        switch (operacion) {
            case SUMAR:       return operando1 + operando2;
            case RESTAR:      return operando1 - operando2;
            case MULTIPLICAR: return operando1 * operando2;
            case DIVIDIR:     return operando1 / operando2;
            case PORCENTAJE:  return operando1 % operando2;
            case IGUAL:       return ( (operando2_str.equals(""))? operando1 : operando2 );
            default:          return operando1;
        }
    }

    // Simbolo de las operaciones
    private String obtenerSimboloOp(OPERACION op) {
        switch(op) {
            case SUMAR: return "+";
            case RESTAR: return "-";
            case MULTIPLICAR: return "x";
            case DIVIDIR: return "÷";
            case RAIZ: return "√";
            case CUADRADO: return "x²";
            case PORCENTAJE: return "%";
            default: return "";
        }
    }

    // Convierte el texto numérico en formato String a un tipo Double seguro
    private double miStringToDouble(String numero_string) {
        if (numero_string.equals(".")) {
            operando2_str="0.";
            display.setText(operando2_str);
            return 0.;
        }
        return Double.parseDouble(numero_string);
    }

    // Formatea un número Double a String
    private String miDoubleToString(double numero_double) {
        return (df.format(numero_double));
    }}