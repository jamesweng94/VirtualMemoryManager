import java.io.*;
import java.util.*;

public class VMM {
	int[] pm;
	BitMap map;
	TLB tlb;
	
	boolean turnOnTLB = true;
	
	VMM(){
		pm = new int [524288];
		for(int i = 0; i < 524288; ++i) {
			pm[i] = 0;
		}
		tlb = new TLB();
		map = new BitMap();
		map.setOne(0, 0);
	}
	
	public void parsingPM(String input1)
	{	
		Scanner s = null;
		try {
			s = new Scanner(new FileReader(input1));
			
			//Page Table setup 
			if(s.hasNextLine()){
				String firstLine = s.nextLine();
				String[] firstTokens = firstLine.split(" ");
				for(int index = 0; index < firstTokens.length; index+=2) {
					int seg = Integer.parseInt(firstTokens[index]);
					int addr = Integer.parseInt(firstTokens[index+1]);
					if(!PTsetup(seg, addr)) {
				//		System.out.print("err ");
					}
				}
			}
			
			//Page Entry setup
			if(s.hasNextLine()){
				String secondLine = s.nextLine();
				String[] secondTokens = secondLine.split(" ");
				for(int i = 0; i < secondTokens.length; i+=3) {
					int pageNum = Integer.parseInt(secondTokens[i]);
					int segNum = Integer.parseInt(secondTokens[i + 1]);
					int addrNum = Integer.parseInt(secondTokens[i+2]);
					
					if(!pageSetup(pageNum, segNum, addrNum)){
					//	System.out.print("err ");
					}
				}
			}
		//	map.print();
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.err.println("Error accessing the source file: \"" + input1);
			System.exit(-2);
		}
	}
	
	public void parsingVA(String input2)
	{
		Scanner sc = null;
		int output = 0;
		
		try {
			sc = new Scanner(new FileReader(input2));
			if(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] tokens = line.split(" ");
				
				for(int i = 0; i < tokens.length; i+=2) {
					int op = Integer.parseInt(tokens[i]);
					
					//make sure integer is not overflow
					long test = Long.parseLong(tokens[i + 1]);
					if(test > Integer.MAX_VALUE || test < Integer.MIN_VALUE) {
						System.out.print("err ");
						continue;
						}
					
					int va = Integer.parseInt(tokens[i + 1]);
					int sp = getSP(va);
					
					int w = get_w(va);
					int f = tlb.search(sp);
										
					if(!turnOnTLB) {
						if(op == 0) {
							read(va, turnOnTLB, output); 
								}
						if(op == 1) {
							write(va, turnOnTLB, output); 
								}
							}
					else {
						if(f == -99) {				// Not in TLB(miss)
							System.out.print("m ");
							if(op == 0) {				
								read(va, turnOnTLB, output);
							}
							if(op == 1) {	
								write(va, turnOnTLB, output);
							}
						}
						else {						//Found a hit
							System.out.print("h ");
							if(op == 0) {	
								output = f + w;
								System.out.print(output + " ");
							}				
							if(op == 1) {
								output = f + w;
								System.out.print(output + " ");							
									}
								tlb.updateFound(sp);
							}
						}
					}
				}
			}	
		catch(IOException e)
		{
			e.printStackTrace();
			System.err.println("Error accessing the source file: \"" + input2);
			System.exit(-2);
		}
	}
	
	public boolean PTsetup(int seg, int pt_addr) {
		if(seg >=512 || pt_addr < 512 && pt_addr != -1) {
			return false;
		}
		pm[seg] = pt_addr;
		if(pt_addr != -1) {
			int frameNum = pt_addr / 512;
			int bitIndex = frameNum / 32;
			frameNum = frameNum % 32;
			map.setOne(bitIndex, frameNum);
			map.setOne(bitIndex, frameNum + 1);
		}
		return true;
	}
	
	public boolean pageSetup(int page, int seg, int pg_addr){
		if(seg >=512 || pm[seg] + page < 512) {
			return false;
		}	
		
		pm[pm[seg] + page] = pg_addr;
		if(pg_addr != -1) {
			int frameNum = pg_addr / 512;		
			int bitIndex = frameNum / 32;
			frameNum = frameNum % 32;
			map.setOne(bitIndex, frameNum);
		}
		
		return true;
	}
	
	public void read(int vir_addr, boolean withTLB, int output) {
		int s = get_s(vir_addr);
		int p = get_p(vir_addr);
		int w = get_w(vir_addr);
		
		if(pm[s] == -1 || pm[pm[s] + p] == -1) {
			System.out.print("pf ");
		}
		else if(pm[s] == 0 || pm[pm[s] + p] == 0) {
			System.out.print("err ");
		}
		else{
			output = pm[pm[s] + p]+ w;
			System.out.print(output + " ");
			if(withTLB) { tlb.updateNoFound(s + p, pm[pm[s] + p]);}
		}
	}
	
	public void write(int vir_addr, boolean withTLB, int output) {
		int s = get_s(vir_addr);
		int p = get_p(vir_addr);
		int w = get_w(vir_addr);
		
		if(pm[s] == -1 || pm[pm[s] + p] == -1) {
			System.out.print("pf ");
		}		
		else if(pm[s] == 0) {
			pm[s] = allocatePT();

			if(pm[pm[s] + p] == 0) {
				pm[pm[s]+ p] = allocatePage();
			}
			
			pm[pm[pm[s]]] = w;
			output = pm[pm[s] + p] + w;
			System.out.print(output + " ");
			if(withTLB) { tlb.updateNoFound(s + p, pm[pm[s] + p]); }
		}
		else if(pm[pm[s] + p] == 0) {
			pm[pm[s]+ p] = allocatePage();		
			output = pm[pm[s] + p] + w;
			System.out.print(output + " ");
			if(withTLB) { tlb.updateNoFound(s + p, pm[pm[s] + p]); }
		}
		else {		
			output = pm[pm[s] + p] + w;
			System.out.print(output + " ");
			if(withTLB) { tlb.updateNoFound(s + p, pm[pm[s] + p]); }
		}
		
	}
	
	int allocatePT() {
		int w_i, w_j;
		map.searchTwo();
		w_i = map.get_i();
		w_j = map.get_j();
		map.setOne(w_i, w_j);
		map.setOne(w_i, w_j+1);
		return w_i * 512 + w_j * 512;
	}
	
	int allocatePage() {
		int w_i, w_j;
		map.searchOne();
		w_i = map.get_i();
		w_j = map.get_j();
		map.setOne(w_i, w_j);	
		return w_i * 512 + w_j * 512;
	}
	
	public int getSP(int va) {
		return get_s(va) + get_p(va);
	}
	
	public int get_s(int va)
	{
		return va<<4>>>23;
	}
	
	public int get_p(int va) {
		return va<<13>>>22;
	}
	
	public int get_w(int va) {
		return va<<23>>>23;
	}
}
