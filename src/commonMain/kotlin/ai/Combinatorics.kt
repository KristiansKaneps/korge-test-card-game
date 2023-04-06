package ai

import game.cards.*

object Combinatorics {
    fun <T> permutations(from: Collection<T>): Collection<Collection<T>> {
        if (from.isEmpty()) return listOf(emptySet())
        return from.flatMap { element ->
            permutations(from - element).map { setOf(element) + it }
        }
    }

    fun <T> variations(from: Collection<T>, k: Int): Collection<Collection<T>> {
        return combinations(from, k).flatMap { combination ->
            permutations(combination)
        }
    }

    fun <T> variationsUpToK(from: Collection<T>, k: Int): Collection<Collection<T>> {
        return (1..k).flatMap { variations(from, it) }
    }

    fun <T> combinations(from: Collection<T>, k: Int): Collection<Collection<T>> {
        if (k == 0) return listOf(emptySet())
        if (from.isEmpty()) return emptySet()
        val head = from.first()
        val tail = from.drop(1)
        return combinations(tail, k - 1).map { setOf(head) + it } + combinations(tail, k)
    }

    fun <T> combinationsUpToK(from: Collection<T>, k: Int): Collection<Collection<T>> {
        return (1..k).flatMap { combinations(from, it) }
    }

    fun groupByCardType(cards: Collection<Card>): Collection<Collection<Card>> {
        return cards.groupBy { it.type }.values
    }

    fun groupByPossibleCardTypeMoves(cards: Collection<Card>, maxSize: Int = cards.size): Collection<Collection<Card>> {
        if(maxSize == 0) return emptyList()
        val groups = cards.groupBy { it.type }.values
        val result = mutableListOf<Collection<Card>>()

        @Suppress("NAME_SHADOWING")
        groups.forEach { cards ->
            if (cards.size > 1) {
                cards.forEach { card -> result.add(setOf(card)) }
                (2 .. maxSize).forEach { k ->
                    combinations(cards, k).forEach { result.add(it) }
                }
            } else {
                result.add(cards)
            }
        }

        return result
    }
}
