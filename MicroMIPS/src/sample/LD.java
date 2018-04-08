package sample;

class LD extends Instruction{

    LD(String line) {
        int in;

        setOPCode("110111");

        String[] reg = line.split(",");

        String[] first = reg[0].split(" ");
        String rt = first[1].replaceAll("\\D+", "");
        in = Integer.parseInt(rt);
        setRt(Integer.toString(in, 2));

        String[] second = reg[1].split("\\(");

        String rs = second[1].replaceAll("\\D+", "");
        in = Integer.parseInt(rs);
        setRs(Integer.toString(in, 2));

        setImm(second[0].replaceAll("\\D+",""));
    }

}
