package stream;

import java.util.Objects;

public class Developer {
	private String name;
	private String language;
	private int salary;

	public Developer(String name, String language, int salary) {
		this.name = name;
		this.language = language;
		this.salary = salary;
	}

	@Override
	public String toString() {
		return "Developer{" +
				"name='" + name + '\'' +
				", language='" + language + '\'' +
				", salary=" + salary +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Developer developer = (Developer) o;
		return salary == developer.salary && name.equals(developer.name) && language.equals(developer.language);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, language, salary);
	}
}
