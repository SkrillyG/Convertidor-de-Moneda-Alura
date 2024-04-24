import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner reading = new Scanner(System.in);
        ConverterRates converter = new ConverterRates("0cbbbf1a0cf766efa89b60be");

        boolean salir = false;
        while (!salir) {
            showMenu();
            int option = reading.nextInt();
            switch (option) {
                case 1:
                    converter.changesCoin("USD", "CRC");
                    break;
                case 2:
                    converter.changesCoin("CRC", "USD");
                    break;
                case 3:
                    converter.changesCoin("BRL", "CRC");
                    break;
                case 4:
                    converter.changesCoin("CRC", "BRL");
                    break;
                case 5:
                    converter.changesCoin("ARS", "USD");
                    break;
                case 6:
                    converter.changesCoin("USD", "ARS");
                    break;
                case 7:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción Incorrecta!!. Por favor, elige una opción del 1 al 7.");
            }
        }

        System.out.println("Gracias por utilizar el Converter de Monedas de AluraLatam!");
    }

    private static void showMenu() {
        System.out.println("""
            *** Convertidor de Moneda Alura ***
            =====================================
            1. Convertid de USD a CRC
            2. Convertir de CRC a USD
            3. Convertir de BRL a CRC
            4. Convertir de CRC a BRL
            5. Convertir de ARS a USD
            6. Convertir de USD a ARS
            7. Salir
            ====================================
            *** Elija una de las Opciones: ***
            """);
    }
}
