const { Classe, Niveau } = require('../models');

const getAll = async (req, res) => {
    try {
        const classes = await Classe.findAll({
            include: [{ model: Niveau }]
        });
        res.status(200).json(classes);
    } catch (error) {
        console.error('Erreur getAll classes:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

module.exports = { getAll };