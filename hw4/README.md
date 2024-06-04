# Домашнее задание №4. Akka Streams

## Описание решаемой задачи

Дан поток целых чисел от 1 до 5.

Необходимо:
- Поток разделить на 3 отдельных потока;
- Все элементы первого потока умножить на 10, второго на 2, третьего на 3;
- Получившиеся элемента из трех отдельных потоков смерджить в один поток с суммированием всех элементов.

## Описание программы:

1. Инициализируем ActorSystem и SystemMaterializer:

```scala
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val materializer: SystemMaterializer = SystemMaterializer(system)
```

2. Задаем источник элементов:

```scala
  val source = Source(1 to 5)
```

3. Для каждого из 3 новых потоков задаем операции преобразования элементов:

```scala
  val multiplyBy10 = Flow[Int].map(_ * 10)
  val multiplyBy2 = Flow[Int].map(_ * 2)
  val multiplyBy3 = Flow[Int].map(_ * 3)
```

4. Задаем выходную функцию:

```scala
  val sink = Sink.foreach(println)
```

5. Настраиваем DSL граф:

Где через GraphDSL.Builder задаем разделение исходного потока на 3 дочерних потока:

```scala
    val bcast = builder.add(Broadcast[Int](3))
```

Задаем функцию слияния 3 потоков:

```scala
 val merge = builder.add(ZipWith((a: Int, b: Int, c: Int) => a + b + c))
```

Настраиваем граф переходов:

```scala
    source ~> bcast ~> multiplyBy10 ~> merge.in0
              bcast ~> multiplyBy2  ~> merge.in1
              bcast ~> multiplyBy3  ~> merge.in2
              merge.out ~> sink
```

## Пример работы программы:

![result.png](images%2Fresult.png)

Исходный код представлен в файле [Application.scala](src%2Fmain%2Fscala%2Fcom%2Fmagnusario%2FApplication.scala)