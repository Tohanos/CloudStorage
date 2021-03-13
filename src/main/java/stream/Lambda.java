package stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Lambda {
	public static void main(String[] args) {
		Callback c = value -> System.out.println(value);

		Callback c1 = System.out::println;

		CallbackDouble cd = (x, y) -> x + " - " + y;

		c.call("test");
		c1.call("test2");
		System.out.println(cd.callDouble("test", "1"));

		Callback c2 = Lambda::testMethod;
		c2.call("example");

		CallbackDouble cd2 = (a, b) -> "1 -" + a + "\n2 - " + b;
		System.out.println(cd2.callDouble("line1", "line2"));

		Consumer<Integer> consumer = a -> {
			a++;
			System.out.println(a);
		};

		consumer = consumer.andThen(arg -> {
			arg *= 2;
			System.out.println(arg);
		});

		consumer.accept(10);

		Predicate<Integer> predicate = value -> value % 2 == 0;
		predicate = predicate.and(value -> value > 6).or(val -> val == 5);
		System.out.println(predicate.test(4));

		Function<Integer, String> converter = "abc"::repeat;
		System.out.println(converter.apply(4));

		Function<String, Integer> convertStringToInt = String::length;
		System.out.println(convertStringToInt.apply("abcde"));

		Supplier<List<Integer>> getList = ArrayList::new;
		System.out.println(getList.get());
	}

	private static void testMethod(String text) {
		System.out.println("!!! " + text + " !!!");
		System.out.println("test1");
		System.out.println("test1");
		System.out.println("test1");
		System.out.println("test1");
	}
}
