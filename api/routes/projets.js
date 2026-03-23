/**
 * @fileoverview Routes des projets
 * @module routes/projets
 */

const express = require('express');
const router = express.Router();
const projetController = require('../controllers/projetController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.get('/all',         verifyToken, checkRole('secretariat'),                              projetController.getAllAdmin);
router.get('/',            verifyToken, checkRole('secretariat', 'proviseur', 'professeur', 'eleve'), projetController.getAll);
router.post('/participer', verifyToken, checkRole('secretariat', 'eleve'),                      projetController.participer);
router.post('/',           verifyToken, checkRole('secretariat', 'eleve'),                      projetController.create);
router.put('/:id/valider', verifyToken, checkRole('secretariat'),                               projetController.valider);
router.delete('/:projetId/participer/:eleveId', verifyToken, checkRole('secretariat', 'eleve'), projetController.retirerParticipation);
router.delete('/:id',      verifyToken, checkRole('secretariat'),                               projetController.remove);

module.exports = router;