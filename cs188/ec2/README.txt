Name: Manohar Jois
EdX: mjois

This was the best I could do in the time I had to put into this. It works decently on layouts where Pacman can stay close to walls, like message.lay and oneHunt.lay using PatrolSLAMAgent.

It doesn't work well in large layouts where Pacman spends lots of time far away from walls. This is because with each time step, more particles are sampled such that they start to spread into a large cloud instead of congregating near walls and corners as Pacman approaches them. With particles in many different locations at once, it's almost impossible to truly distinguish which squares are walls and which are not because the given range readings don't match up with many particles' internal maps.

I attempted to consult a paper titled "Improved Techniques for Grid Mapping with Rao-Blackwellized Particle Filters"* which described methods for improving the particle filter for SLAM. Among them were selective resampling only when the sum of weights of the particles was below a certain threshold, along with resampling from a Gaussian distribution around the local maximum of the range sensor probability given a position and map. However the math was too intense to incorporate into my final implementation.



*Improved Techniques for Grid Mapping with Rao-Blackwellized Particle Filters. Grisetti, Giorgio; Cyrill Stachniss and Wolfram Burgard. http://www2.informatik.uni-freiburg.de/~grisetti/pdf/grisetti06tro.pdf
