
public class BitMap {
	int [] bits;
	int [] mask;
	int [] mask2;
	int search_i;
	int search_j;
	
	BitMap(){
		bits = new int[32];
		mask = new int[32];
		mask2 = new int[32];
		
		//mask initialization
		mask[31] = 1;
		for(int i = 30; i >= 0; i--){
			mask[i] = mask[i+1]<<1;
		}
		
		//mask2 initialization
		for(int i = 0; i < 32; i++) {
			mask2[i] = ~mask[i];
		}
	}
	
	public void setOne(int i, int j)
	{
		bits[i] = bits[i] | mask[j];
	}
	
	public void setZero(int i, int j) {
		bits[i] = bits[i] & mask2[j];
	}
	
	public void print(){
		System.out.println();
		for(int i = 0; i < 32; ++i) {
			System.out.println(Integer.toBinaryString(bits[i]));
		}
	}
	
	public void searchOne(){
		int test;
		for(int i = 0; i < 32; i++) {
			for(int j = 0; j < 32; j++) {
				test = bits[i] & mask[j];
				if(test == 0) {
					search_i = i;
					search_j = j;
					return;
				}
			}
		}
	}
	
	public void searchTwo(){
		int test, test2;
		for(int i = 0; i < 32; i++) {
			for(int j = 0; j < 32; j++) {
				test = bits[i] & mask[j];
				test2 = bits[i] & mask[j+1];
				if(test == 0 && test2 == 0) {
					search_i = i;
					search_j = j;
					return;
				}
			}
		}
	}
	
	
	public int get_i() {
		return search_i;
	}
	
	public int get_j() {
		return search_j;
	}
	
}
