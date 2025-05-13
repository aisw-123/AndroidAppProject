const express = require('express');
const path = require('path');
const axios = require('axios');
const mongoose = require('mongoose');
const crypto = require('crypto');
const bcrypt = require('bcrypt');
const cookieParser = require('cookie-parser');
const app = express();
app.use(cookieParser());
app.use(express.json());
const jwt = require('jsonwebtoken');
const secretKey = 'OcEPiEBCVDnTeftzS6ZtdFYSuHJWOBThdG5jCM5+6MJzMBKMBN9u9PBpuzuLEMBn3GnKxbgT3QSfyq3UWXq5og==';
const PORT = process.env.PORT || 3000;
let artsyToken = null;
let tokenExpiry = null;
const mongoURI = `mongodb+srv://aisw007:6ImxYEvXlTPjmeur@cluster0.6i5149h.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0`;

mongoose.connect(mongoURI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
})
.then(() => console.log('Connected to MongoDB Atlas'))
.catch((err) => console.error('MongoDB error:', err));

const userSchema = new mongoose.Schema({
    fullName: String,
    email: { type: String, unique: true },
    password: String,
    profileImageUrl: { type: String }, 
  });
  const User = mongoose.model('User', userSchema);
  const favoriteArtistSchema = new mongoose.Schema({
    email: { type: String, required: true, unique: true },
    favorites: [
      {
        artist_id: { type: String },
        artist_name: { type: String },
        artist_thumbnail: { type: String },
        dateAdded: { type: Date, default: Date.now }  
      }
    ]
  });

  function getGravatarUrl(email) {
      const hash = crypto.createHash('sha256')
        .update(email.trim().toLowerCase())
        .digest('hex');
          return `https://www.gravatar.com/avatar/${hash}?d=identicon&s=200`;
    }
  
    const verifyToken = (token) => {
      try {
        return jwt.verify(token, secretKey);  
      } catch (err) {
        return null;  
      }
    };
  
    const FavoriteArtist = mongoose.model('FavoriteArtist', favoriteArtistSchema);

const authenticate = async () => {
    try {
        const response = await axios.post("https://api.artsy.net/api/tokens/xapp_token", {
            client_id: "472465794ead6234657d",
            client_secret: "0ae87f652c6db9a06930aa3770d2183a"
        }, {
            headers: { "Content-Type": "application/json" }
        });

        artsyToken = response.data.token;
        tokenExpiry = new Date(response.data.expires_at);
    } catch (error) {
        console.error("Failed to authenticate:", error.message);
    }
};

const ensureAuthToken = async (req, res, next) => {
    if (!artsyToken || new Date() >= tokenExpiry) {
        await authenticate();
    }
    next();
};

app.get('/api/search', ensureAuthToken, async (req, res) => {
    const query = req.query.q;

    if (!query) {
        return res.status(400).json({ error: "Search query empty" });
    }

    try {
        const response = await axios.get("https://api.artsy.net/api/search", {
            headers: { "X-Xapp-Token": artsyToken },
            params: { q: query, size: 10, type: "artist" }
        });

        const results = response.data._embedded?.results || [];
        res.json(results.length > 0 ? results : []);
    } catch (error) {
        res.status(500).json({ error: "Failed to fetch search results from Artsy API." });
    }
});

app.get('/api/artist/:id', ensureAuthToken, async (req, res) => {
    const artistId = req.params.id;
  
    try {
      const artistResponse = await axios.get(`https://api.artsy.net/api/artists/${artistId}`, {
        headers: {
          'X-Xapp-Token': artsyToken
        }
      });
  
      const artist = artistResponse.data;
      res.json({
        name: artist.name || '',
        birthday: artist.birthday || '',
        deathday: artist.deathday || '',
        nationality: artist.nationality || '',
        biography: artist.biography || ''
      });
    } catch (error) {
      res.status(500).json({ error: 'Failed to fetch artist details' });
    }
  });


app.get('/api/artworks/:artistId', (req, res) => {
    const artistId = req.params.artistId;
  
    axios.get(`https://api.artsy.net/api/artworks?artist_id=${artistId}&size=10`, {
      headers: {
        'X-XAPP-Token': artsyToken
      }
    })
    .then(response => {
      const artworks = response.data._embedded?.artworks || [];
      
      const artworkData = artworks.map(artwork => ({
        id: artwork.id,
        title: artwork.title || 'Untitled',  
        date: artwork.date || 'Unknown date',  
        image: artwork._links?.thumbnail?.href || 'assets/artsy_logo.svg'  
      }));
  
      res.json({ artworks: artworkData });
    })
    .catch(error => {
      res.status(500).send({ message: 'Failed to fetch artworks' });
    });
  });
  
  app.get('/api/categories/:artworkId', ensureAuthToken, async (req, res) => {
    const artworkId = req.params.artworkId;

    try {
        const response = await axios.get('https://api.artsy.net/api/genes', {
            headers: {
                'X-Xapp-Token': artsyToken
            },
            params: {
                artwork_id: artworkId,
                size: 10  
            }
        });

        const genes = response.data._embedded?.genes || [];
        console.log(genes)

        if (genes.length === 0) {
            return res.json({ categories: [] });
        }

        const categories = genes.map(gene => ({
            name: gene.name || 'Unnamed Category',
            image: gene._links?.thumbnail?.href || 'assets/artsy_logo.svg',
            description: gene.description || ''
        }));

        res.json({ categories });

    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch categories for artwork' });
    }
});
app.post('/check-email', async (req, res) => {
    const { email } = req.body;
  
    try {
      const existingUser = await User.findOne({ email });
      if (existingUser) {
        return res.status(409).json({ exists: true });
      } else {
        return res.status(200).json({ exists: false });
      }
    } catch (error) {
      console.error('Error checking email:', error);
      res.status(500).json({ error: 'Internal error' });
    }
  });

  app.post('/login', async (req, res) => {
    const { email, password } = req.body;
    //console.log('Full request:', req);

  
    try {
      console.log("Im here 20");
      console.log("Received email", email);
      const user = await User.findOne({ email });
      if (!user) {
        return res.status(400).json({ message: 'Email or password is incorrect' });
      }
     

        const isPasswordValid = await bcrypt.compare(password, user.password);
      if (!isPasswordValid) {
        return res.status(400).json({ message: 'Email or password is incorrect' });
      }
      console.log("After password check");
      console.log("Received email :", email);  
      const token = jwt.sign(
        { fullName: user.fullName, email: user.email, profileImageUrl: user.profileImageUrl },
        secretKey,
        { expiresIn: '1h' }
      );
      console.log('Generated Token:', token);
  
      res.cookie('auth_token', token, {
        httpOnly: true,
        secure: false,
        sameSite: 'Lax',
        maxAge: 3600000 
      });
  
      res.status(200).json({
        message: 'Login successful',
        token: token,
        user: {
          email: user.email,
          fullName: user.fullName,
          profileImageUrl: user.profileImageUrl
        },
      });

    } catch (err) {
      console.error('Error logging in:', err);
      res.status(500).json({ message: 'Server error' });
    }
  });

 app.post('/register', async (req, res) => {
    const { fullName, email, password } = req.body;
  
    const profileImageUrl = getGravatarUrl(email);  
    const hashedPassword = await bcrypt.hash(password, 10);
  
    const token = jwt.sign(
      { fullName, email, profileImageUrl }, 
      secretKey,
      { expiresIn: '1h' }
    );
    console.log('generated token:', token);
  
    const newUser = new User({
      fullName,
      email,
      password: hashedPassword,
      profileImageUrl
    });
    await newUser.save();
    const responsePayload = {
        message: 'User registered successfully',
        token: token,
        user: {
          fullName: newUser.fullName,
          email: newUser.email,
          profileImageUrl: newUser.profileImageUrl,
        },
      };
  
      console.log('Response:', responsePayload);

    const newFavArtistEntry = new FavoriteArtist({
      email: newUser.email,  
      favorites: [] 
    });
    await newFavArtistEntry.save();
  
    res.cookie('auth_token', token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'Lax',
      maxAge: 3600000 
    });
    res.status(201).json({
        message: 'User registered successfully',
        token: token,
        user: {
          fullName: newUser.fullName,
          email: newUser.email,
          profileImageUrl: newUser.profileImageUrl,
        },
      });
    });

    app.post('/api/addFavorite', async (req, res) => {
        const { artist_id, artist_name, artist_thumbnail, email } = req.body;
      
        try {
            console.log('Received addFavorite request:', req.body);
          const userFavArtistEntry = await FavoriteArtist.findOne({ email });
      
          if (!userFavArtistEntry) {
            return res.status(404).json({ message: 'User not found' });
          }
      
          const existingArtist = userFavArtistEntry.favorites.find(
            fav => fav.artist_id === artist_id
          );
      
          if (existingArtist) {
            return res.status(400).json({ message: 'Artist already in favorites' });
          }
      
          userFavArtistEntry.favorites.push({
            artist_id,
            artist_name,
            artist_thumbnail,
            dateAdded: new Date()
          });
      
          await userFavArtistEntry.save();
      
          res.status(200).json({ message: 'Artist added' });
        } catch (error) {
          console.error('Error adding favorites:', error);
          res.status(500).json({ message: 'Internal error' });
        }
      });
      app.post('/api/removeFavorite', async (req, res) => {
        const { artist_id, email } = req.body;
      
        try {
          const userFavArtistEntry = await FavoriteArtist.findOne({ email });
      
          if (!userFavArtistEntry) {
            return res.status(404).json({ message: 'User not found' });
          }
      
          const existingArtist = userFavArtistEntry.favorites.find(
            fav => fav.artist_id === artist_id
          );
      
          if (!existingArtist) {
            return res.status(400).json({ message: 'Artist not found in favorites' });
          }
      
          userFavArtistEntry.favorites = userFavArtistEntry.favorites.filter(
            fav => fav.artist_id !== artist_id
          );
      
          await userFavArtistEntry.save();
      
          res.status(200).json({ message: 'Artist removed from favorites' });
        } catch (error) {
          res.status(500).json({ message: 'Internal server error' });
        }
      });
      app.get('/api/getFavorites', async (req, res) => {
        const email = req.query.email;
        console.log("Received email:", email);
      
        try {
          if (!email || typeof email !== 'string') {
            return res.status(400).json({ message: 'Invalid email' });
          }
      
          const userFavArtistEntry = await FavoriteArtist.findOne({ email });
      
          if (!userFavArtistEntry) {
            return res.status(200).json([]); 
          }
          const artistIds = userFavArtistEntry.favorites.map(fav => fav.artist_id);
          res.status(200).json(artistIds);
      
        } catch (error) {
          console.error('Error fetching favorite artists:', error);
          res.status(500).json({ message: 'Internal server error' });
        }
      });

      app.post('/api/favorite-date', async (req, res) => {
        const { email, artist_id } = req.body;
      
        if (!email || !artist_id) {
          return res.status(400).json({ message: 'Missing email or artist_id' });
        }
      
        try {
          const userFavArtistEntry = await FavoriteArtist.findOne({ email });
      
          if (!userFavArtistEntry) {
            return res.status(404).json({ message: 'User not found' });
          }
      
          const favorite = userFavArtistEntry.favorites.find(
            fav => fav.artist_id === artist_id
          );
      
          if (!favorite) {
            return res.status(404).json({ message: 'Favorite artist not found' });
          }
      
          const now = new Date();
          const addedDate = new Date(favorite.dateAdded);
          const diffInSeconds = Math.floor((now - addedDate) / 1000);
      
          let relativeTime;
      
          if (diffInSeconds < 60) {
            relativeTime = `${diffInSeconds} second${diffInSeconds !== 1 ? 's' : ''} ago`;
          } else if (diffInSeconds < 3600) {
            const minutes = Math.floor(diffInSeconds / 60);
            relativeTime = `${minutes} minute${minutes !== 1 ? 's' : ''} ago`;
          } else {
            const hours = Math.floor(diffInSeconds / 3600);
            relativeTime = `${hours} hour${hours !== 1 ? 's' : ''} ago`;
          }
          console.log(relativeTime)
          res.status(200).json({ relativeTime });
        } catch (error) {
          console.error('Error fetching favorite date:', error);
          res.status(500).json({ message: 'Server error' });
        }
      });
      
      
      
app.delete('/delete', async (req, res) => {
        const { email } = req.body;
        console.log("Im here to delete - test")
      console.log(email)
        if (!email) {
          return res.status(400).json({ message: 'Email is required' });
        }
      
        try {
          const user = await User.findOneAndDelete({ email });
      
          if (!user) {
            return res.status(404).json({ message: 'User not found' });
          }
          const favoriteArtist = await FavoriteArtist.findOneAndDelete({ email });

        if (!favoriteArtist) {
            console.log('No favorite artist entry found for this user.');
        }
          res.status(200).json({ message: 'Account successfully deleted' });
        } catch (err) {
          console.error('Error deleting account:', err);
          res.status(500).json({ message: 'Server error' });
        }
      });

      app.get('/api/similar-artists', async (req, res) => {
        const artistId = req.query.artistId;
      
        if (!artistId) {
          return res.status(400).json({ error: 'Artist ID is required' });
        }
      
        try {
          const response = await axios.get(`https://api.artsy.net/api/artists?similar_to_artist_id=${artistId}`, {
            headers: {
              'X-XAPP-Token': artsyToken
            }
          });
      
          const similarArtists = response.data._embedded.artists.map(artist => ({
            id: artist.id,
            name: artist.name,
            image: artist._links?.thumbnail?.href || 'assets/artsy_logo.svg'
          }));
      
          res.json({ artists: similarArtists });
        } catch (error) {
          console.error('Error fetching similar artists:', error);
          res.status(500).json({ error: 'Failed to fetch similar artists' });
        }
      });

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
