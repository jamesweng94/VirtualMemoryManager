public class TLB {
	Objects [] objs;
	
	TLB(){
		objs = new Objects[4];
		for(int i = 0; i < 4; i++) {
			objs[i] = new Objects();
			objs[i].setLRU(i);
			objs[i].setSP(-1);
			objs[i].setF(-1);
		}
	}
	
	public int search(int sp) {
		for(int i = 0; i < 4; i++) {
			if(sp == objs[i].getSP()) {
				return objs[i].getF();
			}
		}
		return -99;	
	}
	
	public void updateNoFound(int sp, int f) {
		for(int i = 0; i < 4; i++) {
			if(objs[i].getLRU() == 0) {
				objs[i].setLRU(3);
				objs[i].setSP(sp);
				objs[i].setF(f);
				decrementALL(i);
				return;
			}
		}
	}
	
	public void updateFound(int _sp) {
		for(int i = 0; i < 4; i++) {	
			if(objs[i].getSP() == _sp) {		
				for(int j = 0; j < 4; j++) {
					if(objs[j].getLRU() > objs[i].getLRU()) {
						objs[j].setLRU(objs[j].getLRU() - 1);
					}
				}
				objs[i].setLRU(3);
				return;
			}
		}
	}
	
	public void decrementALL(int index) {
		for(int i = 0; i < 4; i++) {
			if(i != index) {
				objs[i].setLRU(objs[i].getLRU()- 1);
			}
		}
	}
	
	public void print() {
		for(int i = 0; i < 4; i++) {
			System.out.println("LRU: " + objs[i].getLRU() + ", SP: " + objs[i].getSP() + ", F: " + objs[i].getF());
		}
		System.out.println();
	}
}
