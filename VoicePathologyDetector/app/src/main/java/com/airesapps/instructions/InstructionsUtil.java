package com.airesapps.instructions;

import java.util.ArrayList;
import java.util.List;

public class InstructionsUtil {

    public final static String INSTRUCTION_1 = "Certifique-se que está em um ambiente silencioso. O detector é sensível a ruídos.";
    public final static String INSTRUCTION_2 = "Certifique-se que está conectado à internet.";
    public final static String INSTRUCTION_3 = "Fale de forma sustentada as vogais \"a\", \"i\" e \"u\" por mais de três segundos.";
    public final static String INSTRUCTION_4 = "O detector necessita de, no mínimo, três segundos de som audível (sem silêncio) para detectar patologias.";
    public final static String INSTRUCTION_5 = "Esta não é uma ferramenta de pré-diagnóstico, somente um profissional da área de saúde pode diagnosticar disfonias. O propósito dessa aplicação é incentivar usuários a buscar assistência médica.";

    public static List<Instruction> getInstructionsList() {
        List<Instruction> instructions = new ArrayList<>();

        instructions.add(new Instruction(INSTRUCTION_1));
        instructions.add(new Instruction(INSTRUCTION_2));
        instructions.add(new Instruction(INSTRUCTION_3));
        instructions.add(new Instruction(INSTRUCTION_4));
        instructions.add(new Instruction(INSTRUCTION_5));

        return instructions;
    }


}
