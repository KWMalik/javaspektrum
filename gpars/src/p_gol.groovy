package gol

import groovyx.gpars.ParallelEnhancer

ParallelEnhancer.enhanceClass(HashSet)

/**
 * @author Michael Hunger 
 * @since 24.03.2010
 */
class GameOfLife {
  static int X=0,Y=1;
  static def env = [[-1,-1],[0,-1],[1,-1],[-1,0],[1,0],[-1,1],[0,1],[1,1]].toSet().makeConcurrent()
  def alive = []

  def neighbours(def cell) {
    this.@env.collect { [cell[X]+it[X],cell[Y]+it[Y]] }.toSet().makeConcurrent()
  }

  def aliveNeighbours(def cell) {
    neighbours(cell).findAll {alive.contains(it) }
  }

  def deadNeighbours(def cell) {
    neighbours(cell).findAll { !alive.contains(it) }
  }

  def haveNeighbourCount(def coll, def counts) {
	coll.findAll { counts.contains(aliveNeighbours(it).size())}
  }

  GameOfLife next() {
    def stayingAlive = haveNeighbourCount(this.@alive, [2,3]).toSet()
    def wakingFromDead = this.@alive.inject([].toSet()) { res,cell ->
                          res += haveNeighbourCount(deadNeighbours(cell),[3])}
    
    new GameOfLife(alive: (stayingAlive + wakingFromDead).makeConcurrent())
  }
  String toString() {
	 (alive.min{it[Y]}[Y]..alive.max{it[Y]}[Y])
		.collect { y ->  
		(alive.min{it[X]}[X]..alive.max{it[X]}[X])
		.collect { x -> 
			alive.contains([x,y]) ? "X" : "."}
			.join("")+"\n" 
			}.join("")
  }
  static GameOfLife fromString(def str) {
	 int x=0,y=0;
	 def alive=[]
     str.each { if (it == 'X') alive+=[[x,y]];
		if (it=='\n') { x=0;y--;}
		else x++;
	 }
     new GameOfLife(alive: alive)	
  }
  static GameOfLife random(count, size) {
	 def rnd = new Random(0L)
	 def alive = ((0..count).collect { [rnd.nextInt(size),rnd.nextInt(size)]}).toSet()
     new GameOfLife(alive: alive.makeConcurrent())	
  }

}
def gol=GameOfLife.fromString(
"""
   X
    X  
    X  
  XXX   

""")

def benchmark = { closure ->  
  start = System.currentTimeMillis()  
  closure.call()  
  now = System.currentTimeMillis()  
  now - start  
}
println benchmark { 1000.times{ gol = gol.next() }}

gol = GameOfLife.random(10000,500)

println benchmark { 100.times{ gol = gol.next() }}
