int k = 5;
int arr[4];

int add(int x, int y) {
	int z ;
	int i =0;
	z = x + y;

	while (i < 4) {
	    if(k==5){
	        arr[i] = 9;
	        z = z + arr[i];
	        ++i;
	    }
	    else {
	        arr[i] = 3;
	        z = z + arr[i];
	        ++i;
	    }
	}
	z = z - k;
	return z;
}

void main () {
	int t = 33;
	_print(add(1, t));
}
