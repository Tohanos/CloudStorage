package stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Streams {
	public static void main(String[] args) throws IOException {
//		List<Developer> developerList = new ArrayList<>();
//		developerList.add(new Developer("name1", "java", 4000));
//		developerList.add(new Developer("name2", "python", 5000));
//		developerList.add(new Developer("name3", "c++", 4500));
//		developerList.add(new Developer("name4", "c++", 4000));
//		developerList.add(new Developer("name5", "python", 3000));
//		developerList.add(new Developer("name6", "java", 4300));
//		developerList.add(new Developer("name5", "python", 3000));
//		developerList.add(new Developer("name6", "java", 4300));
//		developerList.add(new Developer("name7", "c++", 3900));
//		developerList.add(new Developer("name8", "python", 3000));
//		developerList.add(new Developer("name9", "java", 4600));
//		developerList.add(new Developer("name10", "java", 5000));
//
////		List<Developer> filteredDeveloperList = new ArrayList<>();
////		for (Developer developer : developerList) {
////			if (developer.getLanguage().equals("java") && developer.getSalary() > 4000) {
////				filteredDeveloperList.add(developer);
////			}
////		}
////
////		for (Developer developer : filteredDeveloperList) {
////			System.out.println(developer);
////		}
//
//		List<Developer> javaDevList = developerList.stream()
//				.filter(p -> p.getLanguage().equals("java") && p.getSalary() > 4000)
//				.collect(Collectors.toList());
//		javaDevList.forEach(System.out::println);
//
//		developerList.stream()
//				.filter(k -> k.getLanguage().equals("python"))
//				.sorted(Comparator.comparingInt(Developer::getSalary))
//				.forEach(System.out::println);
//
//		double averageSalary = developerList.stream()
//				.filter(d -> d.getLanguage().equals("java"))
//				.mapToInt(Developer::getSalary)
//				.average()
//				.getAsDouble();
//		System.out.println(averageSalary);
//		Developer developerTemp = new Developer("-", "sjklghls", 367430);
//		Developer developer = developerList.stream()
//				.filter(k -> k.getLanguage().equals("js") || k.getLanguage().equals("PHP"))
//				.distinct()
//				.findAny().orElse(null);
//		System.out.println(developer);
//
//
//		boolean java = developerList.stream()
//				.filter(k -> k.getLanguage().equals("java"))
//				.anyMatch(developerTemp::equals);
//		System.out.println(java);
//
//		// Создание стримов
//		Collection<String> collection = Arrays.asList("one", "two", "three");
//		Stream<String> stream = collection.stream();
//
//		Stream.of("one", "two", "three");
//
//		String[] array = {"one", "two", "three"};
//		Arrays.stream(array);
//
//		Files.lines(Path.of("test.txt"));
//
//		IntStream chars = "abcdef".chars();
//
//		Stream.builder().add("one").add("two").build();
//
//		developerList.parallelStream();

//		Map<String, Integer> collect = Files.newBufferedReader(Path.of("src/main/java/stream/test.txt")).lines()
//				.flatMap(line -> Arrays.stream(line.split(" +")))
//				.map(v -> v.replaceAll("[!?.,:;—]", "").toLowerCase(Locale.ROOT))
//				.filter(line -> !line.isBlank())
//				.sorted()
//				.collect(Collectors.toMap(Function.identity(), val -> 1, Integer::sum));
//
//		collect.entrySet().stream()
//				.sorted(Map.Entry.comparingByValue((o1, o2) -> o2 - o1))
//				.forEach(e -> {
//					System.out.println(e.getKey() + " : " + e.getValue());
//				});
//
		List<List<Integer>> lists = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			lists.add(new ArrayList<>());
		}

		for (int i = 0; i < 40; i++) {
			int idx = (int) (Math.random() * 7);
			lists.get(idx).add((int) (Math.random() * 1000));
		}

		System.out.println(lists);

		List<Integer> newList = lists.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		System.out.println(newList);
	}
}
