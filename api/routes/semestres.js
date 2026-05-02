const express = require('express');
const router = express.Router();
const { Semestre } = require('../models');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.get('/', verifyToken, async (req, res) => {
    try {
        const semestres = await Semestre.findAll();
        res.status(200).json(semestres);
    } catch (error) {
        res.status(500).json({ message: 'Erreur serveur' });
    }
});

router.post('/', verifyToken, checkRole('secretariat'), async (req, res) => {
    try {
        const { numero, annee_scolaire } = req.body;
        if (!numero || !annee_scolaire) {
            return res.status(400).json({ message: 'Champs manquants' });
        }
        const semestre = await Semestre.create({ numero, annee_scolaire });
        res.status(201).json(semestre);
    } catch (error) {
        res.status(500).json({ message: 'Erreur serveur' });
    }
});

module.exports = router;