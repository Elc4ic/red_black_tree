import java.io.File


class RBTree(private var root: Node = Node()) {

    enum class Color {
        RED, BLACK
    }

    class Passport(series: Int, number: Int) {
        init {
            if (!(1..9999).contains(series) && !(1..999999).contains(number)) throw Exception()
        }

        var series: Int = series % 10000
        var number: Int = number % 1000000
    }

    data class Node(
        var passport: Passport? = null,
        val duplicates: MutableSet<Int> = mutableSetOf(),
        var color: Color = Color.RED,
        var left: Node? = null,
        var right: Node? = null,
        var parent: Node? = null
    ) {
        constructor(passport: Passport) : this() {
            this.passport = passport
            this.left = Node(color = Color.BLACK)
            this.right = Node(color = Color.BLACK)
        }

        operator fun compareTo(other: Node): Int {
            val result = when {
                this.passport!!.series < other.passport!!.series -> -1
                this.passport!!.series > other.passport!!.series -> 1
                this.passport!!.number < other.passport!!.number -> -1
                this.passport!!.number > other.passport!!.number -> 1
                else -> 0
            }
            return result
        }

        override operator fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Node) return false
            return this.passport?.series == other.passport?.series && this.passport?.number == other.passport?.number
        }

        override fun toString(): String {
            return "${this.passport?.series} ${this.passport?.number} ${this.color} ${this.duplicates}"
        }
    }

    private fun Node?.isNullLeaf() = this != null && passport == null
    private fun Node.uncleL(): Node? = this.parent?.parent?.left
    private fun Node.uncleR(): Node? = this.parent?.parent?.right
    private fun Node.grand(): Node? = this.parent?.parent

    private fun Node.bratR(to: Node? = null): Node? {
        if (to != null) this.parent?.right = to
        return this.parent?.right
    }

    private fun Node.bratL(to: Node? = null): Node? {
        if (to != null) this.parent?.left = to
        return this.parent?.left
    }

    private fun cutNode(before: Node, after: Node) {
        when {
            before.parent.isNullLeaf() -> root = after
            before == before.bratL() -> before.parent?.left = after
            else -> before.parent?.right = after
        }
        after.parent = before.parent
    }

    private fun minimum(node: Node): Node {
        var z = node
        while (!z.left.isNullLeaf()) {
            z = z.left!!
        }
        return z
    }

    private fun leftRotate(x: Node) {
        val y = x.right
        x.right = y?.left
        y?.left.let { it?.parent = x }
        y?.parent = x.parent
        when {
            x.parent.isNullLeaf() -> root = y!!
            x == x.bratL() -> x.bratL(y)
            else -> x.bratR(y)
        }
        y?.left = x
        x.parent = y
    }

    private fun rightRotate(x: Node) {
        val y = x.left
        x.left = y?.right
        y?.right.let { it?.parent = x }
        y?.parent = x.parent
        when {
            y?.parent.isNullLeaf() -> root = y!!
            x == x.bratR() -> x.bratR(y)
            else -> x.bratL(y)
        }
        y?.right = x
        x.parent = y
    }


    fun add(n: Int, series: Int, number: Int) {
        val newNode = Node(Passport(series, number))
        var y = root
        var x = root
        while (!x.isNullLeaf()) {
            y = x
            x = if (newNode == x) {
                x.duplicates.add(n)
                return
            } else (if (newNode > x) x.right else x.left)!!
        }
        if (newNode == y) {
            y.duplicates.add(n)
            return
        } else newNode.duplicates.add(n)
        newNode.parent = y
        if (y.isNullLeaf()) root = newNode
        else if (newNode < y) y.left = newNode
        else y.right = newNode
        fixAdd(newNode)
    }

    private fun fixAdd(node: Node) {
        var z = node
        while (z.parent?.color == Color.RED && z != root) {
            if (z.parent == z.uncleL()) {
                val uncle = z.uncleR()
                if (uncle?.color == Color.RED) {
                    uncle.color = Color.BLACK
                    z.parent?.color = Color.BLACK
                    z.grand()?.color = Color.RED
                    z = z.grand()!!
                } else {
                    if (z == z.bratR()) {
                        z = z.parent!!
                        leftRotate(z)
                    }
                    z.parent?.color = Color.BLACK
                    z.grand()?.color = Color.RED
                    rightRotate(z.grand()!!)
                }
            } else {
                val uncle = z.uncleL()
                if (uncle?.color == Color.RED) {
                    uncle.color = Color.BLACK
                    z.parent?.color = Color.BLACK
                    z.grand()?.color = Color.RED
                    z = z.grand()!!
                } else {
                    if (z == z.bratL()) {
                        z = z.parent!!
                        rightRotate(z)
                    }
                    z.parent?.color = Color.BLACK
                    z.grand()?.color = Color.RED
                    leftRotate(z.grand()!!)
                }
            }
        }
        root.color = Color.BLACK
    }

    fun delete(series: Int, number: Int, n: Int) {
        search(series, number)?.let {
            if (it.duplicates.size > 1) {
                it.duplicates.remove(n)
                return
            }
            delete(it)
        }
    }

    private fun delete(z: Node) {
        var y = z
        val x: Node?
        var originalColor = y.color
        if (z.left.isNullLeaf()) {
            x = z.right
            cutNode(z, z.right!!)
        } else if (z.right.isNullLeaf()) {
            x = z.left
            cutNode(z, z.left!!)
        } else {
            y = minimum(z.right!!)
            originalColor = y.color;
            x = y.right
            if (y.parent == z) {
                x?.parent = y
            } else {
                cutNode(y, y.right!!)
                y.right = z.right
                y.right?.parent = y
            }
            cutNode(z, y)
            y.left = z.left
            y.left?.parent = y
            y.color = z.color
        }
        if (originalColor == Color.BLACK) {
            fixDelete(x);
        }
    }

    private fun fixDelete(node: Node?) {
        var z = node
        while (z != root && z?.color == Color.BLACK) {
            z = fixByBrat(z)(z == z.bratL())
        }
        z?.color = Color.BLACK
    }

    private fun fixByBrat(z: Node): (Boolean) -> Node? = { isLeft ->
        var brat = if (isLeft) z.bratR() else z.bratL()
        val getBrat = { z: Node? -> if (isLeft) z?.bratR() else z?.bratL() }
        val rotate1 = { z: Node -> if (isLeft) leftRotate(z) else rightRotate(z) }
        val rotate2 = { z: Node -> if (isLeft) rightRotate(z) else leftRotate(z) }
        val getSon1 = { z: Node? -> if (isLeft) z?.right else z?.left }
        val getSon2 = { z: Node? -> if (isLeft) z?.left else z?.right }
        if (brat?.color == Color.RED) {
            brat.color = Color.BLACK
            z.parent?.color = Color.RED
            rotate1(z.parent!!)
            brat = getBrat(z)
        }
        if (getSon1(brat)?.color == Color.BLACK && getSon2(brat)?.color == Color.BLACK) {
            brat?.color = Color.RED
            z.parent
        } else {
            if (getSon1(brat)?.color == Color.BLACK) {
                getSon2(brat)?.color = Color.BLACK
                brat?.color = Color.RED
                rotate2(brat!!)
                brat = getBrat(z)
            }
            brat?.color = z.parent?.color!!
            z.parent?.color = Color.BLACK
            getSon1(brat)?.color = Color.BLACK
            rotate1(z.parent!!)
            root
        }
    }

    fun search(series: Int, number: Int): Node? {
        val newNode = Node(Passport(series, number))
        var y: Node? = null
        var x = root
        while (!x.isNullLeaf() && y != newNode) {
            y = x
            x = if (newNode > x) x.right!!
            else x.left!!
        }
        return y
    }

    fun initFromFile(path: String) {
        File(path).bufferedReader().readLines().forEachIndexed { index, s ->
            try {
                val (series, number) = s.split(" ").map { it.toInt() }
                this.add(index + 1, series, number)
            } catch (e: Exception) {
                println("$s is not valid passport")
            }

        }
    }

    fun print() = printTree(root, "")
    fun printWithLeafs() = printTreeWithLeafs(root, "")
    fun lrPrint() = leftRightOrder(root)

    private fun leftRightOrder(root: Node?) {
        if (!root.isNullLeaf()) {
            leftRightOrder(root?.left)
            print("${root?.passport?.series}->")
            leftRightOrder(root?.right)
        }
    }

    private fun printTreeWithLeafs(node: Node?, indent: String) {
        if (node == null) return
        printTree(node.right, "$indent     ")
        println("$indent $node")
        printTree(node.left, "$indent     ")
    }

    private fun printTree(node: Node?, indent: String) {
        if (node.isNullLeaf()) return
        printTree(node?.right, "$indent     ")
        println("$indent $node")
        printTree(node?.left, "$indent     ")
    }
}

fun main() {
    val tree = RBTree()
    tree.initFromFile("src/main/kotlin/tree_passport_db.txt")
    tree.print()
}