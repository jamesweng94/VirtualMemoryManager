
public class Main {

	public static void main(String[] args) {
		
		String input1 = args[0];
		String input2 = args[1];
		
		VMM simulator = new VMM();
		simulator.parsingPM(input1);
		simulator.parsingVA(input2);	
	}
}
