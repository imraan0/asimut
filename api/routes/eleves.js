/**
 * @fileoverview Routes CRUD des élèves
 * @module routes/eleves
 */

const express = require('express');
const router = express.Router();
const eleveController = require('../controllers/eleveController');
const moyenneController = require('../controllers/moyenneController');
const optionController = require('../controllers/optionController');
const professeurController = require('../controllers/professeurController');
const conventionController = require('../controllers/conventionController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');
const { upload } = require('../middlewares/upload');

router.get('/me', verifyToken, checkRole('eleve'), eleveController.getMe);
router.get('/:id/options',        verifyToken, checkRole('secretariat', 'proviseur', 'professeur'),         optionController.getByEleve);
router.get('/:id/moyennes',       verifyToken, checkRole('secretariat', 'proviseur', 'professeur'),         moyenneController.getByEleve);
router.get('/:id/conventions',    verifyToken, checkRole('eleve','secretariat', 'proviseur', 'professeur'), conventionController.getByEleve);
router.post('/:id/referent/auto', verifyToken, checkRole('secretariat'                           ),         professeurController.affecterAuto);
router.post('/:id/referent',      verifyToken, checkRole('secretariat'                           ),         professeurController.affecter);
router.post('/import',            verifyToken, checkRole('secretariat'), upload.single('fichier'),          eleveController.importCSV);
router.get('/',                   verifyToken, checkRole('secretariat', 'proviseur', 'professeur'),         eleveController.getAll);
router.get('/:id',                verifyToken, checkRole('secretariat', 'proviseur', 'professeur'),         eleveController.getById);
router.post('/',                  verifyToken, checkRole('secretariat'),                                    eleveController.create);
router.put('/:id',                verifyToken, checkRole('secretariat'),                                    eleveController.update);
router.delete('/:id',             verifyToken, checkRole('secretariat'),                                    eleveController.remove);

module.exports = router;