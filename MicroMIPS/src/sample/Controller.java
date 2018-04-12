package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML private Button loadButton;

    @FXML private Button resetButton;

    @FXML private TextArea codingArea;

    @FXML private ListView register_list;

    @FXML private TableView<OpcodeTableItem> opcodeTable;

    @FXML private TableColumn<OpcodeTableItem, String> colInstruction;

    @FXML private TableColumn<OpcodeTableItem, String>  colHexOpcode;

    @FXML private TableColumn<OpcodeTableItem, String>  colBit31to26;

    @FXML private TableColumn<OpcodeTableItem, String>  colBit25to21;

    @FXML private TableColumn<OpcodeTableItem, String>  colBit20to16;

    @FXML private TableColumn<OpcodeTableItem, String>  colBit15to11;

    @FXML private TableColumn<OpcodeTableItem, String>  colBit10t06;

    @FXML private TableColumn<OpcodeTableItem, String>  colBit5to0;

    @FXML
    private void showCyclePane() {
        values = FXCollections.observableArrayList();
        for (int i = 0; i < 32; i++)
            registers.put("R" + i, "0000000000000000");

        for (int i = 0; i < registers.size(); i++)
            values.add("R" + i + " = 0000000000000000");

        register_list.setItems(values);
    }

    @FXML
    private void processCode() {
        boolean isValid = false;
        savedCode = codingArea.getText().split("\\n");
        ArrayList<String> labels = new ArrayList<>();

        for (String code: savedCode) {
            if (code.contains(":"))
                labels.add(code.substring(0 ,code.indexOf(":")));
        }

        for (String code: savedCode) {
            String toUse = null;

            if (code.contains(":"))
                toUse = code.substring(code.indexOf(":"));
            else
                toUse = code;

            if (toUse.startsWith("LD")) {
                isValid = checkingLD(code);

                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }

            } else if (toUse.startsWith("SD")) {
                isValid = checkingSD(code);

                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }
            } else if (toUse.startsWith("DADDIU")) {
                isValid = checkingDADDIU(code);

                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }
            } else if (toUse.startsWith("DADDU")) {
                isValid = checkingDADDU(code);

                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }
            } else if (toUse.startsWith("BC")) {
                isValid = checkingBC(labels, code);

                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }
            } else if (toUse.startsWith("BEQC")) {
                isValid = checkingBEQC(labels, code);


                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }
            } else if (toUse.startsWith("XORI")) {
                isValid = checkingXORI(code);

                if (!isValid) {
                    System.out.println("Error in line: " + code);
                    return;
                }
            } else {
                System.out.println("Invalid instruction.");
            }
        }

        makeInstructions();

        opcodeTableItems = new ArrayList<>();

//        for (int i = 0; i < instructions.size(); i++){
//            Instruction ins = instructions.get(i);
//
//            if (ins instanceof DADDIU)
//                opcodeTableItems.add(new OpcodeTableItem(savedCode[i], ins.toHex(), ins.getOPCode(), ins.getRs(),
//                                                         ins.getRt(), ins.getImm().substring(0, 5), ins.getImm().substring(5, 10), ins.getImm().substring(10, 16)));
//            else if (ins instanceof DADDU)
//                opcodeTableItems.add(new OpcodeTableItem(savedCode[i], ins.toHex(), ins.getOPCode(), ins.getRs(),
//                                                         ins.getRt(), ins.getRd(), ins.getSa(), ins.getFunc()));
//            else if (ins instanceof LD)
//                opcodeTableItems.add(new OpcodeTableItem(savedCode[i], ins.toHex(), ins.getOPCode(), ins.getRs(),
//                                                         ins.getRt(), ins.getImm().substring(0, 5), ins.getImm().substring(5, 10), ins.getImm().substring(10, 16)));
//            else if (ins instanceof SD)
//                opcodeTableItems.add(new OpcodeTableItem(savedCode[i], ins.toHex(), ins.getOPCode(), ins.getRs(),
//                                                         ins.getRt(), ins.getImm().substring(0, 5), ins.getImm().substring(5, 10), ins.getImm().substring(10, 16)));
//            else if (ins instanceof XORI)
//                opcodeTableItems.add(new OpcodeTableItem(savedCode[i], ins.toHex(), ins.getOPCode(), ins.getRs(),
//                                                         ins.getRt(), ins.getImm().substring(0, 5), ins.getImm().substring(5, 10), ins.getImm().substring(10, 16)));
//        }
    }

    @FXML
    private void clear() {
        codingArea.clear();
        instructions.clear();
        NPC = 0;
    }

    @FXML
    private void runOneCycle() {
        currPC = NPC;
        System.out.println("IR: " + instructions.get(NPC).toHex());
        NPC += 4;
        System.out.println("NPC: " + NPC);

        A = registers.get("R" + instructions.get(currPC).getIR21to25());
        System.out.println("A: " + A);
        B = registers.get("R" + instructions.get(currPC).getIR16to20());
        System.out.println("B: " + B);
        Imm = instructions.get(currPC).getR15to0();
        System.out.println("IMM: " + Imm);

        performOperation(instructions.get(currPC));
        System.out.println("ALUOUTPUT: " + ALUOutput);
        System.out.println("COND: " + cond);

        if (getNewPC(instructions.get(currPC))) {
            PC = ALUOutput;
            System.out.println("PC: " + PC);
        }
        else
            System.out.println("PC: " + NPC);

        if (instructions.get(currPC) instanceof LD) {
            LMD = ALUOutput;
            System.out.println("LMD: " + LMD);
        } else
            System.out.println("LMD: n/a");

        if (instructions.get(currPC) instanceof SD) {

        } else
            System.out.println("Range: n/a");

        if (instructions.get(currPC) instanceof BC || instructions.get(currPC) instanceof BEQC)
            System.out.println("Rn: n/a");
        else {

        }

        currIns++;
}

    private boolean getNewPC(Instruction instruction) {
        return instruction instanceof BC || instruction instanceof BEQC;
    }

    private void makeInstructions() {
        for(String line : savedCode) {

            String[] temp = line.split("\\s+");

            if (temp[0].indexOf("\\:") > 0) {
                String[] parsed = temp[0].split(" ");
                temp[0] = parsed[1];
            }

            switch (temp[0]) {
                case "LD":
                    instructions.put(NPC, new LD(line));
                    break;

                case "SD":
                    instructions.put(NPC, new SD(line));
                    break;

                case "DADDIU":
                    instructions.put(NPC, new DADDIU(line));
                    break;

                case "DADDU":
                    instructions.put(NPC, new DADDU(line));
                    break;

                case "BC":
                    // TODO fix implementation of this
                    instructions.put(NPC, new BC(line));
                    break;

                case "BEQC":
                    // TODO fix implementation of this and create BEQC class
                    instructions.put(NPC, new BEQC(line));
                    break;

                case "XORI":
                    instructions.put(NPC, new XORI(line));
                    break;
            }
            NPC += 4;
        }
        NPC = 0;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NPC = 0;
        currIns = 0;
        registers = new HashMap<>();
        instructions = new HashMap<>();
    }

    private void performOperation(Instruction ins){
        BigInteger aluOut;
        BigInteger a = new BigInteger(A, 16);
        BigInteger b = new BigInteger(B, 16);
        BigInteger imm = new BigInteger(Imm, 16);

        if (ins instanceof DADDIU) {
            aluOut = a.add(imm);
            ALUOutput = aluOut.toString(16);
            while (ALUOutput.length() < 16){
                if (ALUOutput.charAt(0) >= '8'){
                    ALUOutput = "f" + ALUOutput;
                } else{
                    ALUOutput = "0" + ALUOutput;
                }
            }
            cond = 0;
        } else if (ins instanceof DADDU) {
            aluOut = a.add(b);
            ALUOutput = aluOut.toString(16);
            while (ALUOutput.length() < 16){
                if (ALUOutput.charAt(0) >= '8'){
                    ALUOutput = "f" + ALUOutput;
                } else{
                    ALUOutput = "0" + ALUOutput;
                }
            }
            cond = 0;
        } else if (ins instanceof LD) {
            aluOut = a.add(imm);
            ALUOutput = aluOut.toString(16);
            while (ALUOutput.length() < 16){
                if (ALUOutput.charAt(0) >= '8'){
                    ALUOutput = "f" + ALUOutput;
                } else{
                    ALUOutput = "0" + ALUOutput;
                }
            }
            cond = 0;
        } else if (ins instanceof SD) {
            aluOut = a.add(imm);
            ALUOutput = aluOut.toString(16);
            while (ALUOutput.length() < 16){
                if (ALUOutput.charAt(0) >= '8'){
                    ALUOutput = "f" + ALUOutput;
                } else{
                    ALUOutput = "0" + ALUOutput;
                }
            }
            cond = 0;
        } else if (ins instanceof XORI) {

        } else if (ins instanceof BC) {

        } else if (ins instanceof BEQC) {

        }
    }

    private boolean checkingDADDIU(String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split(",");

        if (splitter.length == 3) {
            try {
                if (splitter[0].contains(":")) {
                    // label
                    codeParts.add(splitter[0].substring(0, splitter[0].indexOf(":")));

                    // rt
                    String[] checker2 = splitter[0].substring(splitter[0].indexOf(":") + 1).trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("DADDIU")) {
                        codeParts.add(checker2[0]);

                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }

                } else {
                    String[] checker2 = splitter[0].trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("DADDIU")) {
                        codeParts.add(checker2[0]);
                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                // for rs
                if (splitter[1].trim().startsWith("R")) {
                    codeParts.add(splitter[1].trim());
                } else {
                    return false;
                }

                String immediate = splitter[2].trim();

                try {
                    if (immediate.startsWith("#")) {
                        Integer toDecimal = Integer.parseInt(immediate.substring(immediate.indexOf("#") + 1), 16);

                        if (!(toDecimal >= 0 && toDecimal <= 65535)) {
                            return false;
                        } else {
                            codeParts.add(immediate.substring(immediate.indexOf("#")));
                        }
                    } else if (immediate.startsWith("0x")) {
                        int toDecimal = Integer.parseInt(immediate.substring(immediate.indexOf("x") + 1), 16);

                        if (!(toDecimal >= 0 && toDecimal <= 65535)) {
                            return false;
                        } else {
                            codeParts.add(immediate.substring(immediate.indexOf("0")));
                        }
                    } else {
                        int toDecimal = Integer.parseInt(immediate.substring(0));
                        codeParts.add(Integer.toString(toDecimal));
                    }
                } catch (Exception e) {
                    return false;
                }

//                for (String c : codeParts)
//                    System.out.println(c);

                // Assuming all is correct
                try {
                    int rt, rs;
                    if (!codeParts.get(0).equalsIgnoreCase("DADDIU")) {
                        rt = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                        rs = Integer.parseInt(codeParts.get(3).substring(codeParts.get(3).indexOf("R") + 1));
                    } else {
                        rt = Integer.parseInt(codeParts.get(1).substring(codeParts.get(1).indexOf("R") + 1));
                        rs = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                    }

                    if ((rt >= 0 && rt <= 31) && (rs >= 0 && rs <= 31))
                        return true;
                } catch (Exception e) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private boolean checkingXORI(String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split(",");

        if (splitter.length == 3) {
            try {
                if (splitter[0].contains(":")) {
                    // label
                    codeParts.add(splitter[0].substring(0, splitter[0].indexOf(":")));

                    // rt
                    String[] checker2 = splitter[0].substring(splitter[0].indexOf(":") + 1).trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("XORI")) {
                        codeParts.add(checker2[0]);

                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }

                } else {
                    String[] checker2 = splitter[0].trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("XORI")) {
                        codeParts.add(checker2[0]);
                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                // for r2
                if (splitter[1].trim().startsWith("R")) {
                    codeParts.add(splitter[1].trim());
                } else {
                    return false;
                }

                String immediate = splitter[2].trim();

                try {
                    if (immediate.startsWith("#")) {
                        Integer toDecimal = Integer.parseInt(immediate.substring(immediate.indexOf("#") + 1), 16);

                        if (!(toDecimal >= 0 && toDecimal <= 65535)) {
                            return false;
                        } else {
                            codeParts.add(immediate.substring(immediate.indexOf("#")));
                        }
                    } else if (immediate.startsWith("0x")) {
                        int toDecimal = Integer.parseInt(immediate.substring(immediate.indexOf("x") + 1), 16);

                        if (!(toDecimal >= 0 && toDecimal <= 65535)) {
                            return false;
                        } else {
                            codeParts.add(immediate.substring(immediate.indexOf("0")));
                        }
                    } else {
                        int toDecimal = Integer.parseInt(immediate.substring(0));
                        codeParts.add(Integer.toString(toDecimal));
                    }
                } catch (Exception e) {
                    return false;
                }

//                for (String c : codeParts)
//                    System.out.println(c);

                // Assuming all is correct
                try {
                    int rt, rs;
                    if (!codeParts.get(0).equalsIgnoreCase("XORI")) {
                        rt = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                        rs = Integer.parseInt(codeParts.get(3).substring(codeParts.get(3).indexOf("R") + 1));
                    } else {
                        rt = Integer.parseInt(codeParts.get(1).substring(codeParts.get(1).indexOf("R") + 1));
                        rs = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                    }

                    if ((rt >= 0 && rt <= 31) && (rs >= 0 && rs <= 31))
                        return true;
                } catch (Exception e) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private boolean checkingLD(String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split(",");

        if (splitter.length == 2) {
            try {
                if (splitter[0].contains(":")) {
                    // label
                    codeParts.add(splitter[0].substring(0, splitter[0].indexOf(":")));

                    // rt
                    String[] checker2 = splitter[0].substring(splitter[0].indexOf(":") + 1).trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("LD")) {
                        codeParts.add(checker2[0]);

                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }

                } else {
                    String[] checker2 = splitter[0].trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("LD")) {
                        codeParts.add(checker2[0]);
                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                //System.out.println(splitter[1].trim());

                String[] offsetBase = splitter[1].trim().split("[\\(\\)]");

                int offset, base;

                offset = Integer.parseInt(offsetBase[0], 16);

                if (!(offset >= 0 && offset <= 255))
                    return false;

                base = Integer.parseInt(offsetBase[1].substring(offsetBase[1].indexOf("R") + 1));

                if (base >= 0 && base <= 31)
                    return true;

            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private boolean checkingSD(String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split(",");

        if (splitter.length == 2) {
            try {
                if (splitter[0].contains(":")) {
                    // label
                    codeParts.add(splitter[0].substring(0, splitter[0].indexOf(":")));

                    // rt
                    String[] checker2 = splitter[0].substring(splitter[0].indexOf(":") + 1).trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("SD")) {
                        codeParts.add(checker2[0]);

                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }

                } else {
                    String[] checker2 = splitter[0].trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("SD")) {
                        codeParts.add(checker2[0]);
                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                //System.out.println(splitter[1].trim());

                String[] offsetBase = splitter[1].trim().split("[\\(\\)]");

                int offset, base;

                offset = Integer.parseInt(offsetBase[0], 16);

                if (!(offset >= 0 && offset <= 255))
                    return false;

                base = Integer.parseInt(offsetBase[1].substring(offsetBase[1].indexOf("R") + 1));

                if (base >= 0 && base <= 31)
                    return true;

            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private boolean checkingDADDU(String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split(",");

        if (splitter.length == 3) {
            try {
                if (splitter[0].contains(":")) {
                    // label
                    codeParts.add(splitter[0].substring(0, splitter[0].indexOf(":")));

                    // rd
                    String[] checker2 = splitter[0].substring(splitter[0].indexOf(":") + 1).trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("DADDU")) {
                        codeParts.add(checker2[0]);

                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }

                } else {
                    String[] checker2 = splitter[0].trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("DADDU")) {
                        codeParts.add(checker2[0]);
                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                // for rs
                if (splitter[1].trim().startsWith("R"))
                    codeParts.add(splitter[1].trim());
                else
                    return false;

                // for rt
                if (splitter[2].trim().startsWith("R"))
                    codeParts.add(splitter[2].trim());
                else
                    return false;

//                for (String c: codeParts)
//                    System.out.println(c);

                try {
                    int rd, rs, rt;
                    if (!codeParts.get(0).equalsIgnoreCase("DADDU")) {
                        rd = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                        rs = Integer.parseInt(codeParts.get(3).substring(codeParts.get(3).indexOf("R") + 1));
                        rt = Integer.parseInt(codeParts.get(4).substring(codeParts.get(4).indexOf("R") + 1));
                    } else {
                        rd = Integer.parseInt(codeParts.get(1).substring(codeParts.get(1).indexOf("R") + 1));
                        rs = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                        rt = Integer.parseInt(codeParts.get(3).substring(codeParts.get(3).indexOf("R") + 1));
                    }

                    if ((rd >= 0 && rd <= 31) && (rt >= 0 && rt <= 31) && (rs >= 0 && rs <= 31))
                        return true;
                } catch (Exception e) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private boolean checkingBC(ArrayList<String> labels, String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split("\\s+");

        try {
            if (splitter.length == 2 && splitter[0].trim().equalsIgnoreCase("BC")) {
                codeParts.add(splitter[0].trim());
                codeParts.add(splitter[1].trim());

                for (String l : labels)
                    if (codeParts.get(1).equalsIgnoreCase(l)) {
                        return true;
                    }

                return false;

            } else if (splitter.length == 3 && splitter[1].trim().equalsIgnoreCase("BC")) {
                codeParts.add(splitter[0].trim().substring(0, splitter[0].indexOf(":")));
                codeParts.add(splitter[1].trim());
                codeParts.add(splitter[2].trim());

//                for (String s : codeParts)
//                    System.out.println(s);

                for (String l : labels)
                    if (codeParts.get(2).equalsIgnoreCase(l)) {
                        return true;
                    }

                return false;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkingBEQC(ArrayList<String> labels, String codeLine) {
        ArrayList<String> codeParts = new ArrayList<String>();
        String[] splitter = codeLine.split(",");

        if (splitter.length == 3) {
            try {
                if (splitter[0].contains(":")) {
                    // label
                    codeParts.add(splitter[0].substring(0, splitter[0].indexOf(":")));

                    // rd
                    String[] checker2 = splitter[0].substring(splitter[0].indexOf(":") + 1).trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("BEQC")) {
                        codeParts.add(checker2[0]);

                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }

                } else {
                    String[] checker2 = splitter[0].trim().split("\\s+");

                    if (checker2[0].equalsIgnoreCase("BEQC")) {
                        codeParts.add(checker2[0]);
                        if (checker2[1].startsWith("R")) {
                            codeParts.add(checker2[1]);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                // for rs
                if (splitter[1].trim().startsWith("R"))
                    codeParts.add(splitter[1].trim());
                else
                    return false;

                // for offset
                //System.out.println(splitter[2].trim());
                codeParts.add(splitter[2].trim());

                try {
                    int rs, rt;
                    boolean flag = false;

                    if (!codeParts.get(0).equalsIgnoreCase("BEQC")) {
                        rs = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));
                        rt = Integer.parseInt(codeParts.get(3).substring(codeParts.get(3).indexOf("R") + 1));

                        //System.out.println(rs + " < " + rt);


                        for (String l : labels)
                            if (codeParts.get(4).equalsIgnoreCase(l))
                                flag = true;

                    } else {
                        rs = Integer.parseInt(codeParts.get(1).substring(codeParts.get(1).indexOf("R") + 1));
                        rt = Integer.parseInt(codeParts.get(2).substring(codeParts.get(2).indexOf("R") + 1));

                        for (String l : labels)
                            if (codeParts.get(3).equalsIgnoreCase(l))
                                flag = true;
                    }

                    if ((rs >= 0 && rs <= 31) && (rt >=0 && rt <= 31) && flag)
                        if ((rs < rt) && (rs != 0) && (rt != 0))
                            return true;
                        else
                            return false;
                } catch (Exception e) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private String[] savedCode;
    private HashMap<String, String> registers;
    private ObservableList<String> values;
    private HashMap<Integer, Instruction> instructions;
    ArrayList<OpcodeTableItem> opcodeTableItems;
    private int currPC;
    private int NPC;
    private static int currIns;
    private String A;
    private String B;
    private String Imm;
    private String ALUOutput;
    private int cond;
    private String PC;
    private String LMD;

}