### 왜 병렬 분산 알고리즘을 사용해야하는가?

### Scaling-out is superior to Scaling-up

- Scale-out : 아주 많은 값싼 서버들을 이용함
- Scale-up : 적은 수의 값비싼 서버들을 이용함
- 데이터 중심(data-intensice) 어플리케이션 분야에서는 아주 많은 값싼 서버들을 많이 이용하는 것을 선호함
- 고가의 서버들은 가격의 관점에서는 선형으로 성능이 증가하지 않음
    
    두 배의 성능의 프로세서 한 개를 가진 컴퓨터의 가격이 일반적인 프로세서 한 개를 가진 컴퓨터 가격의 두 배보다 훨씬 더 비쌈
    

### Why MapReduce?

- 데이터 중심 프로세싱(Data-intensive processing)
    1. 한 대의 컴퓨터의 능력으로 처리가 어려움
    2. 근본적으로 수십대, 수백대 혹은 수천대의 컴퓨터를 묶어서 처리해야 함
    3. 맵리듀스(MapReduce) 프레임워크가 하는 것이 바로 이것
- 맵리듀스는 빅데이터를 이용한 효율적인 계산이 가능한 첫 번째 프로그래밍 모델
    
    기존의 존재하는 여러 가지 다른 병렬 컴퓨팅 방법에서는 프로그래머가 낮은 레벨의 시스템 세부 내용까지 아주 잘 알고 많은 시간을 쏟아야만 함 
    

### MapReduce Framework

- 빅데이터를 이용하는 응용분야에서 최근에 주목 받고 있음
- 값싼 컴퓨터들을 모아서 클러스터를 만들고 여기에서 빅데이터를 처리하기 위한 스케일러블(scalable) 병렬 소프트웨어의 구현 쉽게 할 수 있도록 도와주는 간단한 프로그래밍 모델
- 구글의 맵리듀스(MapReduce) 또는 오픈소스인 하둡(Hadoop)은 맵리듀스 프레임워크(MapReduce Framework)의 우수한 구현의 형태임
    
    구글의 맵리듀스 프레임워크는 내부적으로 사용되는 기술이라 외부에서 사용할 수 없기 때문에 오픈소스인 아파치 하둡을 이용해서 맵리듀스 프레임워크를 구현해서 사용
    
    맵리듀스는 병렬 분산 처리 기법이기 때문에, 하둡 없이도 자체적으로 구현할 수 있다. 그러나 하둡을 활용하면 쉽고 효율적으로 대규모 데이터를 분산 처리할 수 있기 때문에 많이 사용된다.
    
    하둡은 대량의 데이터를 분산 저장하고 처리할 수 있는 오픈소스 프레임워크이다. 여러 대의 컴퓨터(클러스터)를 사용해 데이터를 효율적으로 관리하고 분석할 수 있도록 도와준다. 
    
- 드라이버에 해당하는 메인 함수가 맵(map) 함수와 리듀스(reduce) 함수를 호출해서 처리
    
    

### MapReduce Programming Model

- 함수형 프로그래밍(Functional programming) 언어의 형태
- 유저는 아래 3가지 함수를 구현해서 제공해야 함
    1. Main 함수
        
        Main 함수가 Map 함수와 Reduce 함수를 차례대로 호출
        
        1. Map 함수 : (key1, val1) → [(key2, val2)] (출력값인 key와 value 쌍이 여러개일 수 있음)
        2. Reduce 함수 : (key2, [val2]) → [(key3, val3)]

### 맵리듀스 프레임워크(MapReduce Framework)

- 맵리듀스 프레임워크에서는 각각의 레코드(record) 또는 튜플(tuple)은 키-밸류(KEY, VALUE) 쌍으로 표현됨
- 맵리듀스 프레임워크는 메인(main) 함수를 한 개의 마스터 머신(master machine)에서 수행하는데 이 머신은 맵 함수를 수행하기 전에 전처리를 하거나 리듀스 함수의 결과를 후처리 하는데 사용될 수 있음
    
    맵 함수를 실행하기 전에 데이터를 정리하거나, 맵 함수가 효율적으로 작동할 수 있도록 준비하는 과정이 포함될 수 있음 ex. 입력 데이터를 분할하거나 불필요한 데이터를 필터링 하거나 데이터를 정렬 및 정규화 하거나 등 
    
- 컴퓨팅은 맵(map)과 리듀스(reduce)라는 유저가 정의한 함수 한 쌍으로 이루어진 맵리듀스 페이즈(phase)를 한 번 수행하거나 여러 번 반복해서 수행할 수 있음
- 한 번의 맵리듀스 페이즈는 맵 함수를 먼저 호출하고 그 다음에 리듀스 함수를 호출하는데 때에 따라서는 맵 함수가 끝난 후에 컴바인(combine) 함수를 중간에 수행할 수 있음
- 드라이버에 해당하는 메인(main) 프로그램에서 맵리듀스 페이즈를 수행시킴

### MapReduce Phase (3단계로 수행)

- 맵(Map) 페이즈
    1. 제일 먼저 수행되며 데이터의 여러 파티션(partition)에 병렬 분산으로 호출되어 수행됨
    2. 각 머신마다 수행된 Mapper는 맵 함수가 입력 데이터의 한 줄마다 맵(Map) 함수를 호출함
    3. Map 함수는 (KEY, VALUE)쌍 형태로 결과를 출력하고 여러 머신에 나누어 보내며 같은 KEY를 가진 (KEY, VALUE) 쌍은 같은 머신으로 보내짐
    
    우리 눈에는 데이터가 하나의 큰 파일로 보여지지만 실제로는 잘게 쪼개져서 여러 머신에 분산되어 들어가있음 여러 대의 각 머신의 Mapper가 한 줄마다 맵 함수를 호출함 각 Mapper는 sequential 하게 맵 함수를 호춯하지만 여러 대의 Mapper가 동시에 수행하는 방식으로 병렬 처리를 수행 
    
- 셔플링(Shuffling) 페이즈
    1. 모든 머신에서 맵 페이즈가 다 끝나면 시작됨
    2. 맵 페이즈에서 각각의 머신으로 보내진 (KEY, VALUE) 쌍을 KEY를 이용해서 정렬(Sorting)한 후에 각각의 KEY마다 같은 KEY를 가진 (KEY, VALUE) 쌍을 모아서 밸류-리스트(VALUE-LIST)를 만든 다음에 (KEY, VALUE-LIST) 형태로 KEY에 따라서 여러 머신에 분산해서 보냄
    
- 리듀스(Reduce) 페이즈
    1. 모든 머신에서 셔플링 페이즈가 다 끝나면 각 머신마다 리듀스 페이즈가 시작
    2. 각각의 머신에서는 셔플링 페이즈에서 해당 머신으로 보내진 각각의 (KEY, VALUE-LIST) 쌍마다 리듀스 함수가 호출되며 하나의 리듀스 함수가 끝나면 다음 (KEY, VALUE-LIST)쌍에 리듀스 함수가 호출됨
    3. 출력이 있다면 (KEY, VALUE) 쌍 형태로 출력함 

### Hadoop

- Apache 프로젝트의 맵리듀스 프레임워크의 오픈 소스
- 하둡 분산 파일 시스템(Hadoop Distributed File System, HDFS)
    1. 빅데이터 파일을 여러 대의 컴퓨터에 나누어서 저장하는 기술
    2. 각 파일은 여러 개의 순차적인 블록으로 저장함
    3. 하나의 파일의 각각의 블록은 폴트 톨러런스(fault tolerance)를 위해서 여러 개로 복사되어 여러 머신의 여기저기 저장됨
        
        [폴트 톨러런스]
        
        시스템을 구성하는 부품의 일부에서 결함(fault) 또는 고장(failure)이 발생하여도 정상적 혹은 부분적으로 기능을 수행할 수 있는 것을 말함 
        
- 빅데이터를 수천 대의 값싼 컴퓨터에 병렬 처리하기 위해 분산함

### Hadoop

- 주요 구성 요소들
    1. MapReduce : 소프트웨어의 수행(컴퓨팅)을 분산함
    2. Hadoop Distributed File System(HDFS) : 데이터를 분산함
- 한 개의 Namenode(master)와 여러 개의 Datanode(slaves)
    
    한 개의 머신은 master로 사용하고 나머지는 datanode로 사용 
    
    1. Namenode
        
        파일 시스템을 관리하고 클라이언트가 파일에 접근할 수 있게 함
        
    2. Datanode
        
        컴퓨터에 들어있는 데이터를 접근할 수 있게 함
        
- 자바 프로그래밍 언어로 맵리듀스 알고리즘을 구현

### MapReduce의 함수

- 맵함수
    1. org.apache.hadoop.mapreduce라는 패키지에 있는 Mapper 클래스를 상속 받아서 맵 메소드(method)를 수정
    2. 입력 텍스트 파일에서 라인 단위로 호출되고 입력은 키-밸류(KEY, VALUE-LIST)의 형태
    3. VALUE는 텍스트의 해당 라인 전체가 들어있음

- 리듀스 함수
    1. org.apache.hadoop.mapreduce라는 패키지에 있는 Reducer 클래스를 상속 받아서 reduce 메소드(method)를 수정
    2. 셔플링 페이즈의 출력을 입력으로 받는데 키-밸류(KEY, VALUE-LIST)의 형태
    3. VALUE-LIST는 맵 함수의 출력에서 KEY를 갖는 (KEY, VALUE)쌍들의 VALUE들의 리스트

- 컴바인(Combine) 함수
    1. 리듀스 함수와 유사한 함수인데 각 머신에서 맵 페이즈에서 맵 함수의 출력 크기를 줄여서 셔플링 페이즈와 리듀스 페이즈의 비용을 줄여주는데 사용됨 
    2. 필요에 따라서 사용할 수도 있고 사용하지 않을 수도 있음 

### MapReduce를 이용한 Word Counting 알고리즘

### An Example of Word Counting With MapReduce - Map

mapper들은 map 함수를 호출하여 각 라인마다 수행한 다음 해시 함수를 이용하여 여러 머신에 분산 시켜 보냄

하둡의 input directory를 설정해주면 해당 폴더의 모든 파일들이 데이터로 사용됨  

- 두 개의 머신 M1과 M2가 있고 각 문서는 라인이 한 개만 있다고 가정
- 머신 Mi마다 mapper가 하나씩 수행되고 mapper는 map 함수를 각 라인 하나마다 차례대로 호출함

다섯 개의 Doc이 있을 때 M1이 Doc1,2를 M2가 Doc3,4,5에 대하여 map 함수를 호출하는데 이때 Key는 현재 파일 문서, 앞의 character부터 몇 번째 character인지 등 해당 문서에 대한 정보들이 들어가게 되고 Value에는 현재 라인이 들어감  

value에 대하여 map 함수를 수행 

### An Example of Word Counting With MapReduce - Shuffling

- Map Phase를 거쳐 나온 Key-Value 쌍들을 Key를 기준으로 정렬하여 다시 Key와 Value-List로 반환
- Machine에서 Shuffling Phase가 각자 돌아감

### An Example of Word Counting With MapReduce - Reduce

### Combine 함수

- Map 함수의 결과 크기를 줄여줌
- 각각의 머신에서 Reduce 함수를 이용하는 것처럼 수행됨
- 셔플링 비용을 줄여줌
- 따라서 맵리듀스 알고리즘 디자인에서 사용하는 것이 좋음
- 본 강의는 예제 알고리즘에서 설명의 편의를 위해서 사용하지 않음

 

### An Example of Word Counting with MapReduce - Combine

- Combine이 같은 Key마다 합계를 Value로 만들어 Shuffling Phase를 수행하게 됨
- Combine 함수를 쓰지 않으면 Shuffling Phase가 끝난 후에 value-list가 [1, 1, 1, 1]과 같은 형태였는데 combine 함수를 사용해서 map 함수 호출 이후에 combine이 aggregated result를 만들어내고 이걸 shuffling 하기 때문에 수행 시간을 줄일 수 있고 네트워크 부하도 줄일 수 있다.

### Overview of MapReduce

- Mapper and Reducer
    1. 각 머신에서 독립적으로 수행됨
    2. Mapper는 Map 함수를 Reducer는 Reduce 함수를 각각 수행
- Conbine functions
    1. 각 머신에서 Map 함수가 끝난 다음에 Reduce 함수가 하는 일을 부분적으로 수행
    2. 셔플링 비용과 네트워크 트래픽(Network Traffic)을 감소시킴
- Mapper와 Reducer는 필요하다면 setup() and cleanup()를 수행할 수 있음
    1. setup() : 첫 Map 함수나 Reduce 함수가 호출되기 전에 맨 먼저 수행
        1. 모든 Map 함수들에게 Broadcast해서 전달해야 할 파라미터들 정보를 Main 함수에서 받아오는데 사용
        2. 모든 Map 함수들이 공유하는 자료구조를 초기화하는데 사용
    2. cleanup() : 마지막 Map 함수나 Reduce 함수가 끝나고 나면 수행
        1. 모든 Map 함수들이 공유하는 자료구조의 결과를 출력하는데 사용
- 한 개의 MapReduce job을 수행할 때에 Map 페이즈만 수행하고 중단할 수도 있음

### Partitioner Class

- Map 함수의 출력인 (KEY, VALUE) 쌍이 KEY에 의해서 어느 Reducer(머신)으로 보내질 것인지 정의하는 Class
- 하둡의 기본 타입은 Hash 함수를 Default로 갖고 있어서 KEY에 대한 해시 값에 따라 어느 Reducer로 보낼지를 결정
    - 하둡의 기본 타입(Text, IntWritable, LongWritable, FloatWritable, DoubleWritable)

### Partitioner Class

- Map 함수의 출력 KEY가 IntWritable 타입이고 VALUE는 Text 타입일 때, 아래와 같이 각 reducer에 가게 하려면 Partitioner Class를 수정해야 함

기본 IntWritable 타입은 KEY의 나머지 값을 활용하기 때문에 예를 들어 Reducer1에는 짝수만 들어가게 되고 Reducer2에는 홀수만 들어가게 될 수 있다. 이런 경우 전체의 정렬을 보고 싶은 경우 Reducer1는 짝수만 정렬되어 있고 Reducer2는 홀수만 정렬되어 있어 두 Reducer를 합쳐서 정렬해야 하는 문제가 발생한다. 

Partitioner를 수정해서 KEY가 1 ~ 30이면 Reducer1로, 나머지는 Reducer2로 보내기

**예시**

### MyPartitioner for IntWritable

```java
public static class MyPartitioner extends Partitioner<IntWritable, Text> {
	@Override
	public int getParition(IntWritable key, Text value, int numPartitions) {
		// 이해를 위한 하드 코딩일 뿐 실제 연습할 땐 이렇게 하지 말 것
		int nbOccurences = key.get(); // key의 값을 뽑아냄, java의 int 타입을 반환해줌
		
		if(nbOccurences <= 30) return 0; // 0번 머신으로 보냄
		else return 1; // 1번 머신으로 보냄
	}
}

// Main 함수에 추가
import org.apache.hadoop.mapreduce.Partitioner;

job.setPartitionerClass(MyParitioner.class);
```

`numPartitions` : Reducer의 개수

### MyPartitioner Class

- WordCount.java를 변형하여 아래와 같은 일을 하도록 Wordcountsort.java를 작성
    - Reducer의 개수를 2개로 설정
    - 각 단어의 첫 글자가 ASCII 코드 순서로 a보다 앞에 오는 경우 reducer 0으로 (즉, 결과가 part-r-00000에 찍히도록)
    - 나머지(특수문자 등)는 reducer 1로 (part-r-00001)

- 필요한 함수
    - value.toString() : 하둡의 Text 타입에서 Java의 string 타입으로 변환하여 리턴
    - charAt(0) : 첫 번째 character를 리턴

**예시**

- Map 함수의 출력인 (KEY, VALUE) 쌍이 KEY는 Text이고 VALUE는 IntWritable 일 때 Word의 첫 글자가 a보다 앞에 오는 경우 `reducer 0`으로 나머지(특수문자 등)는 `reducer 1`으로 보냄
    
    
    대문자는 `reducer 0`으로, 소문자는 `reducer 1`로 보내기

**Project/src/Driver.java 파일 수정**

```java
pgd.addClass("wordcountsort", Wordcountsort.class, "A map/reduce program that output frequency of the words in the input files by alphabetical order.");

```

**Project 디렉토리**

```bash
$ ant
$ hdfs dfs -rm -r wordcount_test_out
$ hadoop jar ssafy.jar wordcountsort wordcount_test wordcount_test_out
$ hdfs dfs -cat wordcount_test_out/part-r-00000 | more
$ hdfs dfs -cat wordcount_test_out/part-r-00001 | more
```

### An Example of an Inverted Index

각각의 단어가 어느 문서에서 어느 위치에 있는지 list로 갖고 있는 것을 Inverted Index라고 한다. 

Doc1 : IMF Financial Economics Crisis

Doc2 : IMF Financial Crisis

Doc3 : Harry Economics

Doc4 : Financial Harry Potter Film

Doc5 : Harry Potter Crisis

각 문서마다 작성된 문서의 inverted index

IMF Doc1:1, Doc2:1

Financial Doc1:6, Doc2:6, Doc4:1

…

Harry Doc3:1, Doc4:11, Doc5:1

Potter Doc4:16, Doc5:7 

위와 같은 문서와 inverted index가 있을 때 검색창에 Harry Potter를 검색하게 되면 각각 Harry와 Potter가 공통적으로 나온 문서를 찾은 뒤 Harry 보다 Potter가 뒤에 나오는 문서를 쉽게 찾아서 반환할 수 있다.

이를 맵리듀스 병렬 처리를 통해 만들어보자. 

**예시**

단어마다 해당 문서의 이름과 위치를 Value로 가지도록 Map 함수를 정의하면 된다.

### Building an Inverted Index

- Wordcount.java를 수정하여 inverted index를 생성하는 코드를 InvertedIndex.java 파일 이름으로 작성
- 입력 파일 : 일반 텍스트
- 출력 파일
    
    `단어`: `파일이름:오프셋`, `파일이름:오프셋`, `파일이름:오프셋`
    

**Project/src/Driver.java 파일 수정**

```java
pgd.addClass("inverted", InvertedIndex.class, "A map/reduce program that generates the inverted index using words in the input files");
```

**Project 디렉토리에서**

```bash
$ ant
// 이번에는 리듀스는 함수의 결과를 출력하는 디렉토리를 맵리듀스 코드에서 자동적으로 삭제하도록 구현할 것이므로 삭제를 안 함
$ hadoop jar ssafy.jar inverted wordcount_test invertedindex_test_out
$ hdfs dfs -cat invertedindex_test_out/part-r-00000 | more
$ hdfs dfs -cat invertedindex_test_out/part-r-00001 | more
```

### InvertedIndex.java - Map

```java
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class Wordcount {
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			// 현재(한 줄의) 위치가 시작 지점에서 몇 byte인지 구하기
			// map 함수의 입력 값은 (오프셋, 한 줄 텍스트) 형태로 전달
			long offset = (long) key; // key는 현재 줄의 바이트 오프셋을 의미
			
			while(itr.hasMoreTokens()) {
				String token = itr.nextToken(); // 해당 줄 내의 다음 단어 가져오기
				word.set(token);
				context.write(word, // TODO : 파일명과 현재 위치를 저장한 value);
			}
		}
		
		private String filename;
		protected void setup(Context context) throws IOException, InterruptedException {
			// 현재 사용하는 파일 이름 가져오기
			filename = ((FileSplit)context.getInputSplit()).getPath().getName();
		}
	}
}
```

map의 `Text value` : 입력 파일의 한 줄