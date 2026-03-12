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
    timestamps: true,
    underscored: true,
    createdAt: false //pas de created_at dans ma table, pas besoin
})

module.exports = Eleve