const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Eleve = sequelize.define('eleve', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nom:{
        type: DataTypes.STRING(100),
        allowNull: false
    },
    prenom:{
        type: DataTypes.STRING(100),
        allowNull: false
    },
    identifiant: {
        type: DataTypes.STRING(50),
        allowNull: false,
        unique: true
    }
}, {
    timestamps: false,
    underscored: true
})

module.exports = Eleve