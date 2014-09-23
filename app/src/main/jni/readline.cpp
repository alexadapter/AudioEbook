/* include readline */
#include	"TtsJniDemo.h"

static int	read_cnt = -1;//刚开始可以置为一个负值（我的理解）
static char	*read_ptr;
static char	read_buf[MAXLINE];

static ssize_t my_read(int fd, char *ptr)//每次最多读取MAXLINE个字符，调用一次，每次只返回一个字符
{

	if (read_cnt <= 0) {
	again:
		if ( (read_cnt = fread(read_buf,1, sizeof(read_buf), (FILE*) fd)) < 0) {//如果读取成功，返回read_cnt=读取的字符			if (errno == EINTR)
				goto again;
			return(-1);
		} else if (read_cnt == 0)
			return(0);
		read_ptr = read_buf;
	}

	read_cnt--;//每次递减1，直到<0读完，才执行上面if的命令。
	*ptr = *read_ptr++;//每次读取一个字符，转移一个字符
	return(1);
}

ssize_t
readline(int fd, void *vptr, size_t maxlen)
{
	ssize_t	n, rc;
	char	c, *ptr;

	ptr = (char*)vptr;
	for (n = 1; n < maxlen; n++) {
		if ( (rc = my_read(fd, &c)) == 1) {
			*ptr++ = c;
			if (c == '\n'){
				break;	/* newline is stored, like fgets() */
			}
		} else if (rc == 0) {
			*ptr = 0;
			return(n - 1);	/* EOF, n - 1 bytes were read */
		} else{
			LOGE("readline error");
			return(-1);		/* error, errno set by read() */
		}
	}
	*ptr = 0;	/* null terminate like fgets() */
	//LOGE("returnleng=%d,vptr=%s",maxlen-n,(char*)vptr);
	return(n);
}

ssize_t
readlinebuf(void **vptrptr)
{
	if (read_cnt)
		*vptrptr = read_ptr;
	return(read_cnt);
}
/* end readline */

ssize_t
Readline(int fd, void *ptr, size_t maxlen)
{
	ssize_t		n;
//	int ret = -1;
	if ( (n = readline(fd, ptr, maxlen)) < 0){
		//err_sys("readline error");
		LOGE("Readline error < 0");
		//return ret;
	}
	//clear last cache
	read_cnt = 0;
	read_ptr = 0;
		//return -1;
	return (n);
}

int readMax = 0;
//往上读取行信息
static ssize_t my_read_last(int fd, char *ptr,int maxlen)//每次最多读取MAXLINE个字符，调用一次，每次只返回一个字符
{
	if (read_cnt <= 0) {
		readMax = maxlen > sizeof(read_buf) ? sizeof(read_buf) : maxlen;
	again:
		if ((read_cnt = fread(read_buf,1, readMax, (FILE*) fd)) < 0) {//如果读取成功，返回read_cnt=读取的字符			if (errno == EINTR)
				goto again;
			return(-1);
		} else if (read_cnt == 0)
			return(0);
		read_ptr = read_buf;
	}

	read_cnt--;//每次递减1，直到<0读完，才执行上面if的命令。
	*ptr = *&read_ptr[read_cnt];//每次读取一个字符，转移一个字符
	return(1);
}

ssize_t readlastline(int fd, void *vptr, size_t maxlen)
{
	ssize_t	n, rc;
	char	c;
	int 	ret = -1;
	//第一位是\n
	for (n = maxlen-1; n >= 0; n--) {
		if ((rc = my_read_last(fd, &c,maxlen)) == 1) {
			if (c == '\n'){
				if(n != maxlen-1){//第一个换行跳过..
					break;	/* newline is stored, like fgets() */
				}
			}
		} else if (rc == 0) {
			//ret = (maxlen- n - 1);	/* EOF, n - 1 bytes were read */
			break;
		} else{
			LOGE("readline error");
			return(-1);		/* error, errno set by read() */
		}
	}
<<<<<<< HEAD
	ret = (maxlen- n - 1);
=======
	ret = (maxlen - n - 1);
>>>>>>> save convert project to android-studio, add some,remove build dir
	return ret;
}

ssize_t ReadLastline(int fd, char *ptr, size_t maxlen)
{
	ssize_t		n;
//	int ret = -1;
	if ( (n = readlastline(fd, ptr, maxlen)) < 0){
		//err_sys("readline error");
		LOGE("Readline error < 0");
		//return ret;
	}
<<<<<<< HEAD
	memset(ptr,0,n+1);
=======
	memset(ptr,0,n);
>>>>>>> save convert project to android-studio, add some,remove build dir
	memcpy(ptr,&read_buf[readMax-n],n);
	ptr[n]='\0';
	//LOGE(" n=%d,readMax=%d ReadLastptr=%s",n,readMax,(char*)ptr);
	//clear last cache
	read_cnt = 0;
	read_ptr = 0;
	return (n);
}
