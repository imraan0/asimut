/**
 * @fileoverview Routes des recherches de stage
 * @module routes/stages
 */

const express = require('express');
const router = express.Router();
const rechercheStageController = require('../controllers/rechercheStageController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.get('/recherches/:id',    verifyToken, checkRole('secretariat', 'proviseur', 'professeur', 'eleve'), rechercheStageController.getByEleve);
router.post('/recherches/:id',   verifyToken, checkRole('secretariat', 'eleve'),                            rechercheStageController.create);
router.put('/recherches/:id',    verifyToken, checkRole('secretariat', 'professeur', 'eleve'),              rechercheStageController.update);
router.delete('/recherches/:id', verifyToken, checkRole('secretariat', 'eleve'),                            rechercheStageController.remove);

module.exports = router;