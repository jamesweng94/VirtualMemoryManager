import java.io.*;
import java.util.*;

public class VMM {
	int[] pm;
	BitMap map;
	TLB tlb;
	
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
				int t_size = firstTokens.length;
				int index;
				for(index = 0; index<t_size;) {
					int seg = Integer.parseInt(firstTokens[index]);
					int addr = Integer.parseInt(firstTokens[index+1]);
					if(!PTsetup(seg, addr)) {
						System.out.println("PTsetup error");
					}
					index+=2;
				}
			}
			
			//Page Entry setup
			if(s.hasNextLine()){
				String secondLine = s.nextLine();
				//System.out.println(secondLine);
				String[] secondTokens = secondLine.split(" ");
				int size = secondTokens.length;
				int i;
				for(i = 0; i < size;){
					int pageNum = Integer.parseInt(secondTokens[i]);
					int segNum = Integer.parseInt(secondTokens[i + 1]);
					int addrNum = Integer.parseInt(secondTokens[i+2]);
					
					if(!pageSetup(pageNum, segNum, addrNum)){
						System.out.println("pageSetup error");
					}
					i+=3;
				}
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.err.println("Error accessing the source file: \"" + input1);
			System.exit(-2);
		}
	}
	
	public boolean PTsetup(int seg, int pt_addr) {
	//	if(seg >=512 || pt_addr < 512) {
	//		return false;
	//	}
	//	System.out.println("Seg: " + seg + ", addr: " + pt_addr);
		pm[seg] = pt_addr;
		if(pt_addr != -1) {
			int frameNum = pt_addr / 512;
			int bitIndex = frameNum / 32;
			frameNum = frameNum % 32;
			map.setOne(bitIndex, frameNum);
			map.setOne(bitIndex, frameNum + 1);
		}
	//	map.print();
		return true;
	}
	
	public boolean pageSetup(int page, int seg, int pg_addr){
		//if(seg >=512 || pm[seg] + page < 512) {
		//	return false;
	//	}	
		
		pm[pm[seg] + page] = pg_addr;
		if(pg_addr != -1) {
			int frameNum = pg_addr / 512;		
			int bitIndex = frameNum / 32;
			frameNum = frameNum % 32;
		//	System.out.println(frameNum + " " + bitIndex);
			map.setOne(bitIndex, frameNum);
		}
		
		return true;
	}
	
	public void parsingVA(String input2)
	{
		Scanner sc = null;
		int output;
		
		try {
			sc = new Scanner(new FileReader(input2));
			if(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] tokens = line.split(" ");
				int size = tokens.length;
				
				for(int i = 0; i < size;) {
					int op = Integer.parseInt(tokens[i]);
					int va = Integer.parseInt(tokens[i + 1]);
					int sp = getSP(va);
					int w = get_w(va);
					int f = tlb.search(sp);
					
//Without TLB
/*
						int s = get_s(va);
					//	System.out.println("s: " + s);
						int p = get_p(va);
					//	System.out.println("p: " + p);
						
						//read operation
						if(op == 0) {		
						//	System.out.println("pm[s]: " + pm[s]);
							if(pm[s] == -1 || pm[pm[s] + p] == -1) {
								System.out.print("pf ");
							}
							else if(pm[s] == 0 || pm[pm[s] + p] == 0) {
								System.out.print("err ");
							}
							else{
							//	System.out.println("pm[s]: " + pm[s]);
								output = pm[pm[s] + p]+ w;
								System.out.print(output + " ");
								
							}
						}
						
						//write operation
						if(op == 1) {				
							//allocating page table (1024 words)
							if(pm[s] == 0) {
								int w_i, w_j;
								//map.print();
								map.searchTwo();
								w_i = map.get_i();
								w_j = map.get_j();
								pm[s] = w_i * 512 + w_j * 512;
								map.setOne(w_i, w_j);
								map.setOne(w_i, w_j+1);
								
								//allocating page(512 words)
								if(pm[pm[s]] == 0) {
									map.searchOne();
									w_i = map.get_i();
									w_j = map.get_j();
									pm[pm[s]] = w_i * 512 + w_j * 512;
									map.setOne(w_i, w_j);
								}
								pm[pm[pm[s]]] = w;
								//System.out.println(pm[pm[s]]);
								output = pm[pm[s]];
								System.out.print(output + " ");
							}
							
							else if(pm[s] == -1 || pm[pm[s] + p] == -1) {
								System.out.print("pf ");
							}
							else {				
								output = pm[pm[s] + p] + w;
								System.out.print(output + " ");
							}
						}
					i+=2;
					
					
	*/			
					
//With TLB
			//
					// Not in TLB(miss)
					if(f == -99){
						System.out.print("m ");
						int s = get_s(va);
						//System.out.println("s: " + s);
						int p = get_p(va);
						//System.out.println("p: " + p);
						
						//read operation
						if(op == 0) {				
							if(pm[s] == -1 || pm[pm[s] + p] == -1) {
								System.out.print("pf ");
							}
							else if(pm[s] == 0 || pm[pm[s] + p] == 0) {
								System.out.print("err ");
							}
							else{
								output = pm[pm[s] + p]+ w;
								System.out.print(output + " ");	
								tlb.updateNoFound(s + p, pm[pm[s] + p]);
							}
						}
						
						//write operation
						if(op == 1) {	
							//allocating page table (1024 words)
							if(pm[s] == -1 || pm[pm[s] + p] == -1) {
								System.out.print("pf ");
							}		
							else if(pm[s] == 0) {
								int w_i, w_j;
								//System.out.println("p[" + s+"]: "+ pm[s]);
								//map.print();
								map.searchTwo();
								w_i = map.get_i();
								w_j = map.get_j();
								pm[s] = w_i * 512 + w_j * 512;
								map.setOne(w_i, w_j);
								map.setOne(w_i, w_j+1);
								
								//allocating page(512 words)
								if(pm[pm[s] + p] == 0) {
									map.searchOne();
									w_i = map.get_i();
									w_j = map.get_j();
									
									pm[pm[s]+ p] = w_i * 512 + w_j * 512;
									map.setOne(w_i, w_j);
								}
								pm[pm[pm[s]]] = w;
								output = pm[pm[s] + p] + w;
								System.out.print(output + " ");
								tlb.updateNoFound(s + p, pm[pm[s] + p]);
							}
							else if(pm[pm[s] + p] == 0) {
								int w_i, w_j;
								map.searchOne();
								w_i = map.get_i();
								w_j = map.get_j();
								
								pm[pm[s]+ p] = w_i * 512 + w_j * 512;
								map.setOne(w_i, w_j);
								pm[pm[pm[s]]] = w;
								output = pm[pm[s] + p] + w;
								System.out.print(output + " ");
								tlb.updateNoFound(s + p, pm[pm[s] + p]);
							}
							else {		
								//System.out.println("p[" + s+"]: "+ pm[s]);
								output = pm[pm[s] + p] + w;
								System.out.print(output + " ");
								tlb.updateNoFound(s + p, pm[pm[s] + p]);
							//	map.searchOne();
							//	System.out.println(" i " + map.get_i() + " j: " + map.get_j());
							}
						}
					}	//Found a hit
					else {
						System.out.print("h ");
						if(op == 0) {	
							tlb.updateFound(sp);
							output = f + w;
						//	System.out.println("\nf: "  + f);
						//	System.out.println("w: "  + w);
							System.out.print(output + " ");
						}
						
						if(op == 1) {
							tlb.updateFound(sp);
							output = f + w;
							System.out.print(output + " ");							
						}
					}
			//		tlb.print();
					i+=2;
				//	*/
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
