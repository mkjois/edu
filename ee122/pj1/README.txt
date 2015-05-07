1. Manohar Jois

2. It was difficult to design the data structures to keep track of the full routing table,
   the distance vectors, and the port mappings. I tried to maintain them as dictionaries
   of lists to be kept sorted with after every operation, but I settled for dictionaries
   of dictionaries and just computed the new best route when a new one challenged it.
   Also difficult was the correct handling of link removals, as I ran into a few edge case
   errors.

3. Just as BGP does, we could have routers advertise their entire path to a destination
   instead of just their distance. This would make it very easy to avoid loops of any
   length when computing best paths.
