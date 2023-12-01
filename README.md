# SitnikovProblemSimulation
(Physics, Celestial mechanics, Computational method)

# What is Three body problem?
In physics and classical mechanics, the three-body problem is the problem of taking the initial positions and 
velocities of three point masses and solving for their subsequent motion.

Example gif from Wikipedia:\
![ ](https://upload.wikimedia.org/wikipedia/commons/1/1c/Three-body_Problem_Animation_with_COM.gif)

There is **no general analytical solution** to the three-body problem given by simple algebraic expressions and integrals. 
Moreover, the motion of three bodies is generally non-repeating, except in special cases.

# What is Sitnikov Problem?
The Sitnikov problem is a sub-case of the spatial elliptic **restricted** three-body problem that allows oscillatory type of motions:\
a massless body moves (oscillates) along a straight line that is perpendicular to the orbital plane that is formed by two equally massed primary bodies moving on symmetric Keplerian orbits.

Example gif from Scholarpedia:\
![ ](http://www.scholarpedia.org/w/images/1/15/Sitnikov.gif)


# What does this project do?
By using JavaFX to **visualize** Sitnikov Problem and allow **interactions**, that users are able to change different variables to observe the fate of the system,
(for now) these include:
1) Masses of the bodies (including the massless body)
2) Eccentricity of the orbit
3) Time step (used in Euler's method, the smaller the time step, the more accurate the result which also consumes more time to complete a cycle)
4) Initial velocities of the bodies
5) Initial position (z) of the massless body
